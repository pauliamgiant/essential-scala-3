## Type Class Instances

Type classes in Scala involve the interaction of a number of components. To simplify the presentation we are going to start by looking at *using* type classes before we look at how to *build them ourselves*.

### Ordering

A simple example of a type class is the [`Ordering`](http://www.scala-lang.org/api/current/#scala.math.Ordering) trait. For a type `A`, an `Ordering[A]` defines a comparison method `compare` that compares two instances of `A` by some ordering. To construct an `Ordering` we can use the convenience method `fromLessThan` defined on the companion object.

Imagine we want to sort a `List` of `Int`s. There are many different ways to sort such a list. For example, we could sort from highest to lowest, or we could sort from lowest to highest. There is a method `sorted` on `List` that will sort a list, but to use it we must pass in an `Ordering` to give the particular ordering we want.

Let's define some `Ordering`s and see them in action.

```scala mdoc:silent
import scala.math.Ordering
```

```scala mdoc
val minOrdering = Ordering.fromLessThan[Int](_ < _)

val maxOrdering = Ordering.fromLessThan[Int](_ > _)

List(3, 4, 2).sorted(using minOrdering)

List(3, 4, 2).sorted(using maxOrdering)
```

Here we define two orderings: `minOrdering`, which sorts from lowest to highest, and `maxOrdering`, which sorts from highest to lowest. When we call `sorted` we pass the `Ordering` we want to use. These implementations of a type class are called *type class instances*.

The type class pattern separates the implementation of functionality (the type class instance, an `Ordering[A]` in our example) from the type the functionality is provided for (the `A` in an `Ordering[A]`). *This is the basic pattern for type classes.* Everything else we will see just provides extra convenience.


### Given Values

It can be inconvenient to continually pass the type class instance to a method when we want to repeatedly use the same instance. Scala provides a convenience, called a *given*, that allows us to get the compiler to pass the type class instance for us. Here's an example of use:

```scala mdoc:silent
given ordering: Ordering[Int] = Ordering.fromLessThan[Int](_ < _)
```

```scala mdoc
List(2, 4, 3).sorted

List(1, 7, 5).sorted
```

Note we didn't supply an ordering to `sorted`. Instead, the compiler provides it for us.

We have to tell the compiler which values it is allowed to pass to methods for us. We do this by annotating a value with `given`, as in the declaration `given ordering: Ordering[Int] = ...`. The method must also indicate that it accepts given values. If you look at the [documentation for the `sorted` method on `List`](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List) you see that the single parameter uses the `using` clause. We'll talk more about using parameter lists in a bit. For now we just need to know that we can get the compiler to supply given values to parameters that themselves accept using parameters.

### History of Givens, Using and Implicits

In Scala 2, the mechanism for implicit parameter passing was called *implicits*. Beginning with Scala 3, this feature was redesigned with clearer syntax and renamed to *givens* and *using parameters*.

#### The Scala 2 Approach

In Scala 2, developers used the `implicit` keyword to mark both the definition of values to be automatically passed, and the parameters that should receive them:

```scala
// Scala 2 syntax
implicit val ordering: Ordering[Int] = Ordering.fromLessThan[Int](_ < _)

def sorted[A](implicit ord: Ordering[A]): List[A] = ...
```

The `implicit` keyword served a dual purpose: it marked both the *supply side* (implicit values) and the *demand side* (implicit parameters). This dual meaning could be confusing for newcomers to the language.

#### The Scala 3 Redesign

In Scala 3, the concept was redesigned with two separate keywords that clarify the intent:

- **`given`** declares a value that the compiler should use automatically (replaces `implicit val`)
- **`using`** marks parameters that should receive given values automatically (replaces `implicit` parameters)

```scala
// Scala 3 syntax
given ordering: Ordering[Int] = Ordering.fromLessThan[Int](_ < _)

def sorted[A](using ord: Ordering[A]): List[A] = ...
```

#### They Are Fundamentally the Same

The underlying mechanism is identical between Scala 2's implicits and Scala 3's givens/using:

1. **Resolution**: The compiler searches for a matching value in scope based on type
2. **Scope rules**: Values are discovered in the local scope and companion objects
3. **Non-ambiguity**: The compiler requires exactly one match to avoid errors
4. **Type-driven**: All resolution is based on types, not names
5. **Convenience**: Both mechanisms exist to reduce boilerplate when using type classes

The differences are purely syntactic and improve clarity. Scala 3 also provides better error messages and more flexible given syntax.

#### Backward Compatibility

Scala 3 maintains full compatibility with Scala 2 implicit syntax. You can still write `implicit` in Scala 3 code, though the Scala 3 style using `given` and `using` is preferred for new code.

### Declaring Given Values

We can declare givens using the `given` keyword, which can define values, objects, or definitions. The syntax is:

```scala
given exampleOne: Type = ...
given exampleTwo: Type with
  // members go here
given exampleThree: Type = ...
```

A given value can be declared at the top level or within a surrounding object, class, or trait.

### Given Value Ambiguity

What happens when multiple given values are in scope? Let's ask the console.

```scala mdoc:nest:silent
given minOrdering: Ordering[Int] = Ordering.fromLessThan[Int](_ < _)

given maxOrdering: Ordering[Int] = Ordering.fromLessThan[Int](_ > _)
```

```scala mdoc:fail
List(3, 4, 5).sorted
```

The rule is simple: the compiler will signal an error if there is any ambiguity in which given value should be used.


### Take Home Points

In this section we've seen the basics for using type classes. In Scala, a type class is just a trait. To use a type class we:

- create implementations of that trait, called type class instances; and
- typically we declare the type class instances as given values.

Declaring values as given tells the compiler it can supply them as a parameter to a method call if none is explicitly given. For the compiler to supply a value:

1. the parameter must accept a using clause in the method declaration;
2. there must be a given value available of the same type as the parameter; and
3. there must be only one such given value available.

### Exercises

#### More Orderings

Define an `Ordering` that orders `Int`s from lowest to highest by absolute value. The following test cases should pass.

```scala
assert(List(-4, -1, 0, 2, 3).sorted(absOrdering) == List(0, -1, 2, 3, -4))
assert(List(-4, -3, -2, -1).sorted(absOrdering) == List(-1, -2, -3, -4))
```

<div class="solution">
```scala mdoc:silent
val absOrdering =
  Ordering.fromLessThan[Int]((x, y) => Math.abs(x) < Math.abs(y))
```
</div>

Now make your ordering a given value, so the following test cases work.

```scala
assert(List(-4, -1, 0, 2, 3).sorted == List(0, -1, 2, 3, -4))
assert(List(-4, -3, -2, -1).sorted == List(-1, -2, -3, -4))
```

<div class="solution">
Simply declare the value as given (and make sure it is in scope)

```scala mdoc:nest:silent
given absOrdering: Ordering[Int] =
  Ordering.fromLessThan[Int]((x, y) => Math.abs(x) < Math.abs(y))
```
</div>

#### Rational Orderings

Scala doesn't have a class to represent rational numbers, but we can easily implement one ourselves.

```scala mdoc:silent
final case class Rational(numerator: Int, denominator: Int)
```

Implement an `Ordering` for `Rational` to order rationals from smallest to largest. The following test case should pass.

```scala
assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
       List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
```

<div class="solution">
```scala mdoc:nest:silent
given ordering: Ordering[Rational] =
  Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) <
      (y.numerator.toDouble / y.denominator.toDouble),
  )
```
</div>
