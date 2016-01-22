import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

interface Validation<A> {

  public <B> Validation<B> map(Function<A,B> f);

  public <B> Validation<B> ap(Validation<Function<A,B>> f);

  public Option<A> getValue();

  public Option<List<String>> getErrors();

}

class Success<A> implements Validation<A> {

  private final A value;

  public Success(A value) {
    this.value = value;
  }

  public <B> Validation<B> map(Function<A,B> f) {
    return new Success<B>(f.apply(value));
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    Option<B> ob = f.getValue().map((g) -> g.apply(value));
    Option<Validation<B>> ovb = ob.map((b) -> new Success<>(b));
    return ovb.getOrElse(new Failure<>(f.getErrors().getOrElse(new ArrayList<String>())));
  }

  public Option<A> getValue() {
    return new Some<>(value);
  }

  public Option<List<String>> getErrors() {
    return new None<>();
  }

  public String toString() {
    return "Success(" + value.toString() + ")";
  }

}

class Failure<A> implements Validation<A> {

  private final List<String> errors;

  public Failure(String error) {
    this.errors = Arrays.asList(error);
  }

  public Failure(List<String> errors) {
    this.errors = errors;
  }

  public <B> Validation<B> map(Function<A,B> f) {
    return new Failure<>(errors);
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    List<String> errors2 = new ArrayList<String>();
    errors2.addAll(errors);
    errors2.addAll(f.getErrors().getOrElse(new ArrayList<String>()));
    return new Failure<>(errors2);
  }

  public Option<A> getValue() {
    return new None<>();
  }

  public Option<List<String>> getErrors() {
    return new Some<>(errors);
  }

  public String toString() {
    return String.format("Failure(%s)", errors);
  }

}

public class ValidationDemo {

  public static Validation<Integer> parseInt(String value) {
    try {
      return new Success<>(Integer.parseInt(value));
    } catch (Exception e) {
      return new Failure<>(String.format("could not parse \"%s\" as an integer", value));
    }
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
      "parseInt(\"6\").ap(parseInt(\"7\").map((x) -> (y) -> x * y)) = " +
      parseInt("6").ap(parseInt("7").map((x) -> (y) -> x * y))
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
      "parseInt(\"6\").ap(parseInt(\"seven\").map((x) -> (y) -> x * y)) = " +
      parseInt("6").ap(parseInt("seven").map((x) -> (y) -> x * y))
    );
    System.out.println(
      "parseInt(\"six\").ap(parseInt(\"7\").map((x) -> (y) -> x * y)) = " +
      parseInt("six").ap(parseInt("7").map((x) -> (y) -> x * y))
    );
    System.out.println(
      "parseInt(\"six\").ap(parseInt(\"seven\").map((x) -> (y) -> x * y)) = " +
      parseInt("six").ap(parseInt("seven").map((x) -> (y) -> x * y))
    );

  }

}
