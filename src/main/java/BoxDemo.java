import java.util.function.Function;

class Box<A> {

  private final A value;

  public Box(A value) {
    this.value = value;
  }

  public <B> Box<B> map(Function<A,B> f) {
    return flatMap((value) -> new Box<>(f.apply(value)));
  }

  public <B> Box<B> flatMap(Function<A,Box<B>> f) {
    return f.apply(value);
  }

  public A getOrElse(A y) {
    return this.value;
  }

  public String toString() {
    return "Box(" + value.toString() + ")";
  }

}

public class BoxDemo {

  public static void main(String[] args) {
    System.out.println("box(6) map multiply(7) = " +
      new Box<Integer>(6).map((x) -> x * 7));
    System.out.println("box(6) flatMap multiplyAndBox(7) = " +
      new Box<Integer>(6).map((x) -> new Box<>(x * 7)));
  }

}

