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

A generic data structure `Foo<A>` is&#42; a monad when:

* A `Foo<X>` can be instantiated given any value of type `X`

  ```java
  class Foo<A> {
    public Foo(A a) { ... }
  }
  ```

* Any function of type `(X) -> Foo<Y>` an be applied to a `Foo<X>` to
  get a `Foo<Y>`

  ```java
  class Foo<A> {
    public Foo(A a) { ... }
    public <B> Foo<B> flatMap(Function<A,Foo<B>> f) { ... }
  }
  ```

## &#42; *Is* a monad vs. *has* a monad

Since Java is an OO language, we can build the constructor and `flatMap`
method right into the `Foo` class, and say that `Foo` *is* a monad.

In languages with ad-hoc polymorphism (e.g.  Haskell, Scala), we tend
pull these out into module functions that act on `Foo` instances, and
say that `Foo` *has* a monad.

This is also possible in Java:

```java
class FooMonad {
  public static <A> Foo<A> pure<A>(A a) {
    return new Foo<>(a);
  }
  public static <A,B> Foo<B> flatMap<A>(Foo<A> a, Function<A, Foo<B>> f) {
    // ...
  }
}
```

We'll use the first approach here, because it's a bit more readable for
folks with an OO background.

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
```

An `Option<A>` might contain an `A`.

```java
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
```

`Some<A>` is an `Option<A>` that contains an `A`.

```java
class None<A> implements Option<A> {

  public A getOrElse(A fallback) {
    return fallback;
  }

  public String toString() {
    return "None";
  }

}
```

`None<A>` is an `Option<A>` that does not contain an `A`.

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
```

An `Option<A>` can be transformed into an `Option<B>` either by applying
a function `(A) -> B` or by applying a function `(A) -> Option<B>`.

```java
class Some<A> implements Option<A> {

  // ...

  public <B> Option<B> map(Function<A,B> f) {
    return new Some<>(f.apply(value));
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return f.apply(value);
  }

}
```

`Some<A>` has an `A`, so we can apply `f` to it in `map` and `flatMap` to
get a new `Some<B>`.

```java
class None<A> implements Option<A> {

  // ...

  public <B> Option<B> map(Function<A,B> f) {
    return new None<>();
  }

  public <B> Option<B> flatMap(Function<A,Option<B>> f) {
    return new None<>();
  }

}
```

`None<A>` has no `A` to operate on, so `map` and `flatMap` just return
`None<B>`s directly.

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
```

A `Validation<A>` might contain a value `A`, or it might contain a list
of errors.  In practice, this would be a list of instances of an `Error`
class that we define.  For simplicity we'll just use `String`s.

```java
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
```

A `Success<A>` contains a value `A`, and no errors.

```java
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

A `Failure<A>` contains a list of errors, and no value `A`.

Now let's add `map` and `ap`.

```java
interface Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f);

  public <B> Validation<B> ap(Validation<Function<A,B>> f);

}
```

A `Validation<A>` can be transformed into a `Validation<B>` either by
applying a function `(A) -> B` or by applying a function `(A) ->
Validation<B>`.

```java
class Success<A> implements Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f) {
    return new Success<B>(f.apply(value));
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    Option<B> ob = f.getValue().map((g) -> g.apply(value));
    Option<Validation<B>> ovb = ob.map((b) -> new Success<>(b));
    return ovb.getOrElse(new Failure<>(f.getErrors().getOrElse(new ArrayList<String>())));
  }

}
```

`Success::map` returns a new `Success`, but `Success:flatMap` could
fail, since `f` is itself a `Validation`.

```java
class Failure<A> implements Validation<A> {

  // ...

  public <B> Validation<B> map(Function<A,B> f) {
    return new Failure<>(errors);
  }

  public <B> Validation<B> ap(Validation<Function<A,B>> f) {
    List<String> errors2 = new ArrayList<String>();
    errors2.addAll(errors);
    errors2.addAll(f.getErrors().getOrElse(new ArrayList<String>()));
    return new Failure<>(errors2);
  }

}
```

Doing anything with a `Failure` returns a new `Failure`, but since it
includes a list of errors, we can add to that list when we encounter
additional errors.

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

## If you're not squinting yet

Let's look at the monadic approach to dependency injection.

## Reader

```java
class Reader<A,B> {

  private final Function<A,B> f;

  public Reader(Function<A,B> f) {
    this.f = f;
  }

  public B run(A x) {
    return f.apply(x);
  }
}
```

`Reader` is a wrapper around a function `f`.  Think of it as an
abstraction of a function.

```java
class Reader<A,B> {

  // ...

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
```

`Reader` lets us compose the wrapped function with other stuff to build
more complex function abstractions.

## Function abstraction

Consider a familiar-looking `Database` class that lets us get and set
some data in a database:

```java
class Database {

  private Map<Long, Double> balances = new HashMap<>(); // imaginary SQL table

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
```

Let's also add some transaction methods that we'll use later:

```java
class Database {

  // ...

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

}
```

Now consider a use case in which we need to get data from the database,
and then use that information to set some data in the database.  This
needs to happen in a single logical transaction to ensure consistency.

Normally we would need to do some state gymnastics to get the
transaction right.  We would make some global lock variable, or a
`ThreadLocal` with a reference to a transaction.  Nasty.

With `Reader`, we simply build up an abstraction of a single logical
function that gets data, does something, then sets data, all as one
operation.

```java
class DatabaseReader<A> extends Reader<Database, A> {

  public DatabaseReader(Function<Database, A> f) {
    super(f);
  }

}
```

A `DatabaseReader<A>` is a `Reader<Database, A>`, but that's not all:

```java
class DatabaseReader<A> extends Reader<Database, A> {

  // ...

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

}
```

When we run a database action, we want it to happen within the context
of a transaction, so `DatabaseReader` has a customized `run`
implementation that starts a transaction, runs the underlying
database-dependent function, then commits (or rolls back) a transaction.

When composing `DatabaseReader`s together, we don't want to lose our
custom `run` implementation, so we have a few more things to override --
any function in `Reader` that constructs a new `Reader`:

```java
class DatabaseReader<A> extends Reader<Database, A> {

  // ...

  @Override
  public <B> Reader<Database,B> map(Function<A,B> g) {
    return new DatabaseReader<B>((x) -> g.apply(f.apply(x)));
  }

  @Override
  public <B> Reader<Database,B> flatMap(Function<A,Reader<Database,B>> g) {
    return new DatabaseReader<B>((x) -> g.apply(f.apply(x)).f.apply(x));
  }

}
```

Now we can use `DatabaseReader` to build up arbitrarily complex
sequences of database actions, and when ready, run them all together in
a single transaction.

Let's make a few useful database actions that we can use later.

```java
class DatabaseReader<A> extends Reader<Database, A> {

  // ...

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
```

## Bonus, not bogus

Let's look at `ReaderDemo`.  We'll start with some utility functions for
formatting money.

```java
public static String formatUSD(Double amount) {
  return "$" + String.format("%1$,.2f", amount);
}

public static double logBalance(double balance) {
  System.out.println("balance: " + formatUSD(balance));
  return balance;
}
```

Now let's put together a database action.

```java
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
```

`readerDemo` is a `DatabaseReader<Double>`, which is a `Reader<Database,
Double>`.  This means it's a big abstraction of a bunch of functions
that need a `Database` to run, and when that happens, it finally returns
a `Double`.

So it's an abstraction.  How do we make it concrete?  How do we make all
of these `Database`-dependent functions run, and run within a
transaction?  We use `DatabaseReader::run`:

```java
Database db = new Database();

readerDemo.run(db);
// starting transaction
// balance: $0.00
// balance: $100.00
// balance: $110.00
// committing transaction
```

## Deimos

Make sure you have Java 8 installed:

```
$ java -version
openjdk version "1.8.0_60"
```

Then fire up sbt and run the demos:

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
starting transaction
balance: $0.00
balance: $100.00
balance: $110.00
committing transaction
```
