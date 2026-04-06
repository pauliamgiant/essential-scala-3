## Given Conversions

So far we have seen two programming patterns using givens: *type enrichment*, which we implement using *extension methods*, and *type classes*, which we implement using *given values and using parameter lists*.

Scala has a third feature called *given conversions* using the `Conversion` type that we will cover here for completeness. Given conversions can be seen as a more general form of extension methods, and can be used in a wider variety of contexts.

<div class="callout callout-warning">
#### The Dangers of Given Conversions {-}

As we shall see later in this section, undisciplined use of given conversions can cause as many problems as it fixes for the beginning programmer.

We recommend using extension methods and given values/using parameters over given conversions wherever possible. By sticking to the type enrichment and type class design patterns you should find very little cause to use given conversions in your code.

You have been warned!
</div>

### Given Conversions

Given conversions are a more general form of extension methods. We can define a `given Conversion[A, B]` to allow the compiler to automatically convert from type `A` to type `B`:

```scala mdoc:silent
class B:
  def bar = "This is the best method ever!"

class A

given Conversion[A, B] with
  def apply(in: A): B = new B()
```

```scala mdoc:warn
new A().bar
```

Extension methods are actually just a more specialized pattern for conversions. With extension methods we define methods on types; with given conversions we can convert from any type to any other type as long as a conversion is available in scope.

### Designing with Given Conversions

The power of given conversions tends to cause problems for newer Scala developers. We can easily define very general type conversions that play strange games with the semantics of our programs:

```scala mdoc:silent
given Conversion[Int, Boolean] with
  def apply(int: Int): Boolean = int == 0
```

```scala mdoc:warn

if 1 then "yes" else "no"

if 0 then "yes" else "no"
```

This example is ridiculous, but it demonstrates the potential problems conversions can cause. The conversion could be defined in a library in a completely different part of our codebase, so how would we debug the bizarre behaviour of the `if` expressions above?

Here are some tips for designing using given conversions that will prevent situations like the one above:

 - Wherever possible, stick to the type enrichment and type class programming patterns.

 - Wherever possible, use extension methods, given values, and using parameters over given conversions.

 - Package conversions clearly, and bring them into scope only where you need them. We recommend organizing them in appropriate namespaces.

 - Avoid creating given conversions that convert from one general type to another general type---the more specific your types are, the less likely the conversion is to be applied incorrectly.

### Exercises

#### Given Conversion from Class

Any extension method can be reimplemented as a class paired with a given conversion. Re-implement the `IntOps` class from the *type enrichment* section in this way. Verify that the class still works the same way as it did before.

<div class="solution">
Here is the solution. The methods `yeah` and `times` are exactly as we implemented them previously. The only differences are the use of a `given Conversion` to perform the conversion:

```scala mdoc:silent
object IntImplicits:
  class IntOps(n: Int):
    def yeah() =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit) =
      for i <- 0 until n do func(i)

  given Conversion[Int, IntOps] with
    def apply(value: Int): IntOps =
      new IntOps(value)
```

The code still works the same way it did previously. The conversion is not available until we bring it into scope:

```scala mdoc:fail
5.yeah()
```

Once the conversion has been brought into scope, we can use `yeah` and `times` as usual:

```scala mdoc:silent
import IntImplicits.given
```

```scala mdoc:warn
5.yeah()
```
</div>
