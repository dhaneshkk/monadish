import java.util.function.Function;

interface Option<A> {

  public <B> Option<B> flatMap(Function<A,Option<B>> f);

  public <B> Option<B> map(Function<A,B> f);

  public A getOrElse(A x);

}

class Some<A> implements Option<A> {

  private final A x;

  public Some(A x) {
    this.x = x;
  }

  public <B> Option<B> map(Function<A,B> f) {
    return new Some<B>(f.apply(x));
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return f.apply(x);
  }

  public A getOrElse(A y) {
    return this.x;
  }

  public String toString() {
    return "Some(" + x.toString() + ")";
  }

}

class None<A> implements Option<A> {

  public <B> Option<B> map(Function<A,B> f) {
    return new None<B>();
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return new None<B>();
  }

  public A getOrElse(A x) {
    return x;
  }

  public String toString() {
    return "None";
  }

}

public class OptionDemo {

  public static Option<Integer> parseInt(String value) {
    try {
      return new Some<>(Integer.parseInt(value));
    } catch (Exception e) {
      return new None<>();
    }
  }

  public static Option<Integer> parseAndMultiply(String value1, String value2) {
    return parseInt(value1).
             flatMap((x) ->
               parseInt(value2).
                 map((y) -> x * y));
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      String value1 = args[0];
      String value2 = args[1];
      Option<Integer> product = parseAndMultiply(value1, value2);
      System.out.println(
        String.format("parseAndMultiply(\"%s\", \"%s\") = %s",
                      value1, value2, product));
    } else {
      System.out.println("error: two arguments required");
    }
  }

}

