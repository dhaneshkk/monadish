This is an informal introduction to monads using Java 8.

Java 8 is not a great language for an introduction to monads, but it's
the most widely readable by the audience, so it will do.

We'll back our way into monad comprehension by starting with the
implementation, and working in reverse through the laws, and toward the
theory.

## What's in the box?

Let's start with a simple, if not very useful, data structure called
`Box<A>`:

```java
class Box<A> {

  private final A x;

  public Box(A x) {
    this.x = x;
  }

}
```

A `Box<A>` just holds a value of type `A`.

## The shape of a monad

Let's make it a monad.

```java
class Box<A> {

  private final A x;

  public Box(A x) {
    this.x = x;
  }

  public <B> Box<B> flatMap(Function<A,Box<B>> f) {
    return f.apply(x);
  }

}
```

We added `flatMap`.  Yep, that's it.  To be a monad, `Box` needs two
things:

1. A way to construct a `Box<A>` given an `A`
1. A way to apply a function of type `(A) -> Box<B>` to get a `Box<B>`

Because `Box` is so simple, it's hard to come up with compelling,
practical examples of when we would need to apply a function of type
`(A) -> Box<B>`, but bear with me -- later examples will do a better job
of this.

Here's an example anyway:

```java
new Box<Integer>(6).map((x) -> new Box<>(x * 7))); // Box(42)
```

For now, assume that it's common to need to apply functions of this
shape to data structures like `Box`.

## 50,000 watts of functor

You may have encountered *mappable* data structures in other languages.
It turns out that every monad is also a functor.

If you have both:

1. A way to construct a `Box<A>` given an `A`
1. A way to apply a function of type `(A) -> Box<B>` to get a `Box<B>`

You can write a function `map`:

```java
public <B> Box<B> map(Function<A,B> f) {
  return flatMap((x) -> new Box<>(f.apply(x)));
}
```

Now you can take a `Box<A>`, apply a function `(A) -> B`, and get a
`Box<B>`.

Here's an example:

```java
new Box<Integer>(6).map((x) -> x * 7); // Box(42)
```

## What else?

That's pretty much it.  There's some other junk to learn, such as the
monad laws, but that's about all there is to it.

The rest of your journey will be gaining familiarity with different
common monads, and developing an intuition for how to put them to work.

## Demos

### Prerequisites

* Java 8

### `Box`

```
$ ./sbt "run-main BoxDemo"
box(6) map multiply(7) = Box(42)
box(6) flatMap multiplyAndBox(7) = Box(42)
```

### `Option`

```
$ ./sbt "run-main OptionDemo 6 7"
parseAndMultiply("6", "7") = Some(42)
```

```
$ ./sbt "run-main OptionDemo six 7"
parseAndMultiply("six", "7") = None
```

```
$ ./sbt "run-main OptionDemo 6 several"
parseAndMultiply("6", "several") = None
```

### `Validation`

```
$ ./sbt "run-main ValidationDemo 6 7"
parseAndMultiply("6", "7") = Success(42)
```

```
$ ./sbt "run-main ValidationDemo six 7"
parseAndMultiply("six", "7") = Failure([could not parse "six" as an integer])
```

```
$ ./sbt "run-main ValidationDemo six seven"
parseAndMultiply("six", "seven") = Failure([could not parse "six" as an integer, could not parse "seven" as an integer])
```

### `Reader`

```
$ ./sbt "run-main ReaderDemo"
Balance: $0.00
Balance: $6.00
Balance: $42.00
```
