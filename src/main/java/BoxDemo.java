import java.util.function.Function;

class Box<A> {

  private final A x;

  public Box(A x) {
    this.x = x;
  }

  public <B> Box<B> map(Function<A,B> f) {
    return flatMap((x) -> new Box<>(f.apply(x)));
  }

  public <B> Box<B> flatMap(Function<A,Box<B>> f) {
    return f.apply(x);
  }

  public A getOrElse(A y) {
    return this.x;
  }

  public String toString() {
    return "Box(" + x.toString() + ")";
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

