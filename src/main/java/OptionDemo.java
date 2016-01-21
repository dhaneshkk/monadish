import java.util.function.Function;

interface Option<A> {

  public A getOrElse(A fallback);

  public <B> Option<B> flatMap(Function<A,Option<B>> f);

  public <B> Option<B> map(Function<A,B> f);

}

class Some<A> implements Option<A> {

  private final A value;

  public Some(A value) {
    this.value = value;
  }

  public A getOrElse(A y) {
    return this.value;
  }

  public <B> Option<B> map(Function<A,B> f) {
    return new Some<B>(f.apply(value));
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return f.apply(value);
  }

  public String toString() {
    return "Some(" + value.toString() + ")";
  }

}

class None<A> implements Option<A> {

  public A getOrElse(A fallback) {
    return fallback;
  }

  public <B> Option<B> map(Function<A,B> f) {
    return new None<B>();
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return new None<B>();
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

    System.out.println(
      "parseInt(\"6\") = " +
      parseInt("6")
    );
    System.out.println(
      "parseInt(\"6\").map((x) -> x * 7) = " +
      parseInt("6").map((x) -> x * 7)
    );
    System.out.println(
      "parseInt(\"6\").flatMap((x) -> parseInt(\"7\").map((y) -> x * y)) = " +
      parseInt("6").flatMap((x) -> parseInt("7").map((y) -> x * y))
    );

    System.out.println(
      "parseInt(\"six\") = " +
      parseInt("six")
    );
    System.out.println(
      "parseInt(\"six\").map((x) -> x * 7) = " +
      parseInt("six").map((x) -> x * 7)
    );
    System.out.println(
      "parseInt(\"6\").flatMap((x) -> parseInt(\"seven\").map((y) -> x * y)) = " +
      parseInt("6").flatMap((x) -> parseInt("seven").map((y) -> x * y))
    );
    System.out.println(
      "parseInt(\"six\").flatMap((x) -> parseInt(\"7\").map((y) -> x * y)) = " +
      parseInt("six").flatMap((x) -> parseInt("7").map((y) -> x * y))
    );
    System.out.println(
      "parseInt(\"six\").flatMap((x) -> parseInt(\"seven\").map((y) -> x * y)) = " +
      parseInt("six").flatMap((x) -> parseInt("seven").map((y) -> x * y))
    );

  }

}

