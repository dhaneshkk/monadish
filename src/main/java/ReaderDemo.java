import java.util.function.Function;
import java.util.Map;
import java.util.HashMap;

class Reader<A,B> {

  protected final Function<A,B> f;

  public Reader(Function<A,B> f) {
    this.f = f;
  }

  public B run(A x) {
    return f.apply(x);
  }

  public <C> Reader<A,C> map(Function<B,C> g) {
    return new Reader<A,C>((x) -> g.apply(f.apply(x)));
  }

  public <C> Reader<A,C> flatMap(Function<B,Reader<A,C>> g) {
    return new Reader<A,C>((x) -> g.apply(f.apply(x)).f.apply(x));
  }

  public <C> Reader<A,C> andThen(Reader<A,C> r) {
    return flatMap((x) -> r);
  }

}

class Database {

  private Map<Long, Double> balances = new HashMap<>(); // imaginary SQL table

  public long beginTransaction() {
    System.out.println("starting transaction");
    // imaginary SQL code here
    return 1L; // imaginary transaction id
  }

  public void commitTransaction(long transactionId) {
    System.out.println("committing transaction");
    // imaginary SQL code here
  }

  public void rollbackTransaction(long transactionId) {
    System.out.println("rolling back transaction");
    // imaginary SQL code here
  }

  public Double getBalance(long accountId) {
    // imaginary SQL code here
    if (balances.containsKey(accountId)) {
      return balances.get(accountId);
    } else {
      return 0D;
    }
  }

  public Double setBalance(long accountId, double newBalance) {
    // imaginary SQL code here
    Double oldBalance = getBalance(accountId);
    balances.put(accountId, newBalance);
    return oldBalance;
  }

}

class DatabaseReader<A> extends Reader<Database, A> {

  public DatabaseReader(Function<Database, A> f) {
    super(f);
  }

  @Override
  public A run(Database db) {
    long transactionId = db.beginTransaction();
    try {
      A a = f.apply(db);
      db.commitTransaction(transactionId);
      return a;
    } catch (Exception e) {
      db.rollbackTransaction(transactionId);
      throw e;
    }
  }

  @Override
  public <B> Reader<Database,B> map(Function<A,B> g) {
    return new DatabaseReader<B>((x) -> g.apply(f.apply(x)));
  }

  @Override
  public <B> Reader<Database,B> flatMap(Function<A,Reader<Database,B>> g) {
    return new DatabaseReader<B>((x) -> g.apply(f.apply(x)).f.apply(x));
  }

  public static DatabaseReader<Double> getBalance(long accountId) {
    return new DatabaseReader<>((db) -> db.getBalance(accountId));
  }

  public static DatabaseReader<Double> setBalance(long accountId, double balance) {
    return new DatabaseReader<>((db) -> db.setBalance(accountId, balance));
  }

  public static Reader<Database, Double> awardBonus(long accountId, double amount) {
    return DatabaseReader.getBalance(accountId).
      flatMap((x) -> DatabaseReader.setBalance(accountId, x + amount));
  }

}

public class ReaderDemo {

  public static String formatUSD(Double amount) {
    return "$" + String.format("%1$,.2f", amount);
  }

  public static double logBalance(double balance) {
    System.out.println("balance: " + formatUSD(balance));
    return balance;
  }

  public static void main(String[] args) {

    long accountId = 1;

    Reader<Database, Double> readerDemo =
      DatabaseReader.getBalance(accountId).
        map(ReaderDemo::logBalance).
        andThen(DatabaseReader.setBalance(accountId, 100.0)).
        andThen(DatabaseReader.getBalance(accountId)).
        map(ReaderDemo::logBalance).
        andThen(DatabaseReader.awardBonus(accountId, 10.0)).
        andThen(DatabaseReader.getBalance(accountId)).
        map(ReaderDemo::logBalance);

    Database db = new Database();

    readerDemo.run(db);

  }

}
