import java.util.function.Function;
import java.util.Map;
import java.util.HashMap;

class Reader<A,B> {

  final Function<A,B> f;

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

  public Double getBalance(long accountId) {
    if (balances.containsKey(accountId)) {
      return balances.get(accountId);
    } else {
      return 0D;
    }
  }

  public Double setBalance(long accountId, double newBalance) {
    Double oldBalance = getBalance(accountId);
    balances.put(accountId, newBalance);
    return oldBalance;
  }

}

class DatabaseReader {

  public static Reader<Database, Double> getBalance(long accountId) {
    return new Reader<>((db) -> db.getBalance(accountId));
  }

  public static Reader<Database, Double> setBalance(long accountId, double balance) {
    return new Reader<>((db) -> db.setBalance(accountId, balance));
  }

}

public class ReaderDemo {

  public static String formatUSD(Double amount) {
    return "$" + String.format("%1$,.2f", amount);
  }

  public static double logBalance(double balance) {
    System.out.println("Balance: " + formatUSD(balance));
    return balance;
  }

  public static void main(String[] args) {

    long accountId = 1;

    Reader<Database, Double> readerDemo =
      DatabaseReader.getBalance(accountId).
        map(ReaderDemo::logBalance).
        andThen(DatabaseReader.setBalance(accountId, 6.0)).
        andThen(DatabaseReader.getBalance(accountId)).
        map(ReaderDemo::logBalance).
        flatMap((balance) ->
          DatabaseReader.setBalance(accountId, balance * 7.0)).
        andThen(DatabaseReader.getBalance(accountId)).
        map(ReaderDemo::logBalance);

    readerDemo.run(new Database());

  }

}
