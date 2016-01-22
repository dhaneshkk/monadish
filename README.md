This is a very informal introduction to monads using Java 8.

Java is not a great language for an introduction to monads, but it's
the most widely readable by the audience, so it will do.

Seriously, this is going to be really tedious in Java.  We're doing it
for the familiarity.  Try not to let it sour you on the underlying
ideas.

We'll back our way into monad comprehension by starting with the
implementation, and working in reverse through the laws, and toward the
theory.

## TL;DR

A generic data structure `Foo<A>` is a monad when:

* A `Foo<X>` can be instantiated given any value of type `X`

    ```java
    class Foo<A> {
      public Foo(A a) { ... }
    }
    ```

* Any function of type `(X) -&gt; Foo<Y>` an be applied to a `Foo<X>` to
  get a `Foo<Y>`

    ```java
    class Foo<A> {
      public Foo(A a) { ... }
      public <B> Foo<B> flatMap(Function<A,Foo<B>> f) { ... }
    }
    ```

## What's in the box?

Let's start with a simple, if not very useful, data structure called
`Box`:

```java
class Box<A> {

  private final A value;

  public Box(A value) {
    this.value = value;
  }

}
```

A `Box<A>` just holds a value of type `A`.  It doesn't even make the
valuable visible to anyone.  How selfish.

## The shape of a monad

Let's make it a monad.

```java
class Box<A> {

  private final A value;

  public Box(A value) {
    this.value = value;
  }

  public <B> Box<B> flatMap(Function<A,Box<B>> f) {
    return f.apply(value);
  }

}
```

We added `flatMap`.  Yep, that's it.  To be a monad, `Box` needs two
things:

1. A way to construct a `Box<A>` given an `A`
1. A way to apply a function of type `(A) -> Box<B>` to a `Box<A>` to
   get a `Box<B>`

Because `Box` is so simple, it's hard to come up with compelling,
practical examples of when we would need to apply a function of type
`(A) -> Box<B>`, but bear with me -- later examples will do a better job
of this.

Here's an example anyway:

```java
new Box<Integer>(6).map((x) -> new Box<>(x * 7)));
// Box(42)
```

For now, assume that it's common to need to apply functions of this
shape to data structures like `Box`.

## 50,000 watts of functor

You may have encountered *mappable* data structures in other languages.
These are functors.  It turns out that every monad is also a functor.

If you have both:

1. A way to construct a `Box<A>` given an `A`
1. A way to apply a function of type `(A) -> Box<B>` to a `Box<A>` to
   get a `Box<B>`

You can write a function `map`:

```java
public <B> Box<B> map(Function<A,B> f) {
  return flatMap((x) -> new Box<>(f.apply(x)));
}
```

This generic implementation will work for any monad.  If we want to, we
can clean it up (and optimize it) by foregoing `flatMap`:

```java
public <B> Box<B> map(Function<A,B> f) {
  return new Box<>(f.apply(value));
}
```

Now you can take a `Box<A>`, apply a function `(A) -> B`, and get a
`Box<B>`.

Here's an example:

```java
new Box<Integer>(6).map((x) -> x * 7);
// Box(42)
```

## What else?

That's pretty much it.  There's some other junk to learn, such as the
monad laws, but that's about all there is to it.

The rest of your journey will be gaining familiarity with different
common monads, and developing an intuition for how to put them to work.

## This is dumb

Yeah, I agree.  I wouldn't really use `Box` for anything.  But it's a
nice, clean way to show what `flatMap` looks like.

## Think outside the `Box`

Let's look at some actually useful monads.

## Call me `Option`

`Option`, also known as `Maybe`, is a collection of either zero or one
elements.  It has two implementations: `None` for when it's empty, and
`Some` for when it contains an element.

Here's an implementation without any of them fancy monads:

```java
interface Option<A> {
  public A getOrElse(A fallback);
}

class Some<A> implements Option<A> {

  private final A value;

  public Some(A value) {
    this.value = value;
  }

  public A getOrElse(A y) {
    return this.value;
  }

  public String toString() {
    return "Some(" + value.toString() + ")";
  }

}

class None<A> implements Option<A> {

  public A getOrElse(A fallback) {
    return fallback;
  }

  public String toString() {
    return "None";
  }

}
```

Now let's add `flatMap` implementations.

We'll also add `map`, but remember that it's not very interesting when
we already have `flatMap`, because we can implement `map` using
`flatMap` and a constructor.

```java
interface Option<A> {

  // ...

  public <B> Option<B> map(Function<A,B> f);

  public <B> Option<B> flatMap(Function<A,Option<B>> f);

}

class Some<A> implements Option<A> {

  // ...

  public <B> Option<B> map(Function<A,B> f) {
    return new Some<B>(f.apply(value));
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return f.apply(value);
  }

}

class None<A> implements Option<A> {

  // ...

  public <B> Option<B> map(Function<A,B> f) {
    return new None<B>();
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return new None<B>();
  }

}
```

## `s/Exception/Option/`

Let's parse some integers, and try to do stuff with them.

```java
public static Option<Integer> parseInt(String value) {
  try {
    return new Some<>(Integer.parseInt(value));
  } catch (Exception e) {
    return new None<>();
  }
}
```

```java
parseInt("6");
// Some(6)

parseInt("six");
// None
```

## `Option::map`

```java
parseInt("6").map((x) -> x * 7);
// Some(42)

parseInt("six").map((x) -> x * 7);
// None
```

## `Option::flatMap`

```java
parseInt("6").flatMap((x) -> parseInt("7").map((y) -> x * y));
// Some(42)

parseInt("6").flatMap((x) -> parseInt("seven").map((y) -> x * y));
// None

parseInt("six").flatMap((x) -> parseInt("7").map((y) -> x * y))
// None

parseInt("six").flatMap((x) -> parseInt("seven").map((y) -> x * y))
// None
```

## `None`!  What it is good for?

In the above examples, we can safely try to parse and multiply some
numbers, but if any of them fail to parse, all we get back is a `None`,
so we don't know much about what went wrong.

It would be nice to know whether the first number failed to parse, or
the second number failed to parse, or both of them failed to parse.  We
need more information than simply `None`, and we need to collect that
information as we go.

## More like mehnad

It turns out we don't need `flatMap` to do this.  That is to say: we
don't need a monad.  We need an applicative functor.

## Like unto `map`

Functors give us `map`:

```java
class Foo<A> {
  // ...
  public <B> Foo<B> map(Function<A,B> f);
}
```

Applicatives build on functors with a new function, `ap`:

```java
class Foo<A> {
  // ...
  public <B> Foo<B> ap(Foo<Function<A,B>> f);
}
```

While a functor lets us apply a raw function to the value encapsulated
by an instance of `Foo`, an applicative lets us apply a function that is
itself encapsulated by an instance of `Foo`.

## `Validation`

`Validation`, like `Option`, has two implementations.  One contains a
value in the case of some successful operation, and the other contains a
list of errors in the case of zero or more failures.

Let's start without `map` and `ap`.

```java
interface Validation<A> {

  public Option<A> getValue();

  public Option<List<String>> getErrors();

}

class Success<A> implements Validation<A> {

  private final A value;

  public Success(A value) {
    this.value = value;
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
```

Now let's add `map` and `ap`.

```java
interface Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f);

  public <B> Validation<B> ap(Validation<Function<A,B>> f);

}

class Success<A> implements Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f) {
    return new Success<B>(f.apply(value));
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    Option<B> ob = f.getValue().map((g) -> g.apply(value));
    Option<Validation<B>> ovb = ob.map((b) -> new Success<>(b));
    return ovb.getOrElse(new Failure<B>(f.getErrors().getOrElse(new ArrayList<String>())));
  }

}

class Failure<A> implements Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f) {
    return new Failure<B>(errors);
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    List<String> errors2 = new ArrayList<String>();
    errors2.addAll(errors);
    errors2.addAll(f.getErrors().getOrElse(new ArrayList<String>()));
    return new Failure<B>(errors2);
  }

}
```

## `s/Exception/List<String>/`

```java
public static Validation<Integer> parseInt(String value) {
  try {
    return new Success<>(Integer.parseInt(value));
  } catch (Exception e) {
    return new Failure<>(String.format("could not parse \"%s\" as an integer", value));
  }
}
```

```java
parseInt("6") = Success(6)

parseInt("six") = Failure([could not parse "six" as an integer])
```

## `Validation::map`

```java
parseInt("6").map((x) -> x * 7);
// Success(42)

parseInt("six").map((x) -> x * 7);
// Failure([could not parse "six" as an integer])
```

## `Validation::ap`

```java
parseInt("6").ap(parseInt("7").map((x) -> (y) -> x * y));
// Success(42)

parseInt("6").ap(parseInt("seven").map((x) -> (y) -> x * y));
// Failure([could not parse "seven" as an integer])

parseInt("six").ap(parseInt("7").map((x) -> (y) -> x * y));
// Failure([could not parse "six" as an integer])

parseInt("six").ap(parseInt("seven").map((x) -> (y) -> x * y));
// Failure([could not parse "six" as an integer, could not parse "seven" as an integer])
```

## Deimos

Make sure you have Java 8 installed, then fire up sbt:

```
$ ./sbt "run-main BoxDemo"
box(6) map multiply(7) = Box(42)
box(6) flatMap multiplyAndBox(7) = Box(42)
```

```
$ ./sbt "run-main OptionDemo"
parseInt("6") = Some(6)
parseInt("6").map((x) -> x * 7) = Some(42)
parseInt("6").flatMap((x) -> parseInt("7").map((y) -> x * y)) = Some(42)
parseInt("six") = None
parseInt("six").map((x) -> x * 7) = None
parseInt("6").flatMap((x) -> parseInt("seven").map((y) -> x * y)) = None
parseInt("six").flatMap((x) -> parseInt("7").map((y) -> x * y)) = None
parseInt("six").flatMap((x) -> parseInt("seven").map((y) -> x * y)) = None
```

```
$ ./sbt "run-main ValidationDemo 6 7"
parseInt("6") = Success(6)
parseInt("6").map((x) -> x * 7) = Success(42)
parseInt("6").ap(parseInt("7").map((x) -> (y) -> x * y)) = Success(42)
parseInt("six") = Failure([could not parse "six" as an integer])
parseInt("six").map((x) -> x * 7) = Failure([could not parse "six" as an integer])
parseInt("6").ap(parseInt("seven").map((x) -> (y) -> x * y)) =
  Failure([could not parse "seven" as an integer])
parseInt("six").ap(parseInt("7").map((x) -> (y) -> x * y)) =
  Failure([could not parse "six" as an integer])
parseInt("six").ap(parseInt("seven").map((x) -> (y) -> x * y)) =
  Failure([could not parse "six" as an integer, could not parse "seven" as an integer])

```

```
$ ./sbt "run-main ReaderDemo"
Balance: $0.00
Balance: $6.00
Balance: $42.00
```
