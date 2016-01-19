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
    return ovb.getOrElse(new Failure<B>(f.getErrors().getOrElse(new ArrayList<String>())));
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
    return new Failure<B>(errors);
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    List<String> errors2 = new ArrayList<String>();
    errors2.addAll(errors);
    errors2.addAll(f.getErrors().getOrElse(new ArrayList<String>()));
    return new Failure<B>(errors2);
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

  public static Validation<Integer> parseAndMultiply(String value1, String value2) {
    return parseInt(value1).ap(
           parseInt(value2).map(
           (x) -> (y) -> x * y));
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      String value1 = args[0];
      String value2 = args[1];
      Validation<Integer> product = parseAndMultiply(value1, value2);
      System.out.println(
        String.format("parseAndMultiply(\"%s\", \"%s\") = %s",
                      value1, value2, product));
    } else {
      System.out.println("error: two arguments required");
    }
  }

}
