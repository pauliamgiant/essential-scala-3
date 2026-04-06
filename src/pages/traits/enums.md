## Enums

We have just seen how to model "this or that" data with a sealed trait and final case classes or case objects. Scala 3 adds a more concise way to define such a type: *enums*. An enum groups the type and all its variants in one place.

For example, returning to our feline example, we can define a simple sum type for a few kinds of feline:

```scala
enum Feline:
  case Lion, Tiger
```

This is equivalent to a sealed trait with two case objects. The cases `Lion` and `Tiger` are values of type `Feline`. We use them in pattern matching the same way as with sealed traits:

```scala
def sound(f: Feline): String =
  f match
    case Feline.Lion  => "roar"
    case Feline.Tiger => "roar"
```

```scala
sound(Feline.Lion)
// res0: String = "roar"
sound(Feline.Tiger)
// res1: String = "roar"
```

The compiler knows all the cases of `Feline`, so it will warn us if we miss one in a pattern match.

<div class="callout callout-info">
#### Enum Syntax {-}

Define a sum type and its cases in one place:

```scala
enum Name:
  case Case1, Case2, ...
```

Cases are values of type `Name`. Pattern matching is exhaustive: the compiler warns if a case is missing.
</div>

### Enum Cases with Parameters

When a case needs to carry data, we give it parameters. The enum then plays the same role as a sealed trait with case classes:

```scala
enum FelineWithData:
  case Lion, Tiger
  case GingerTom(favouriteFood: String)
```

Here `Lion` and `Tiger` are parameterless cases; `GingerTom` has a single field. Pattern matching uses the same constructor-style syntax we saw with case classes:

```scala
def dinner(feline: FelineWithData): String =
  f match
    case Lion       => "springbok"
    case Tiger      => "mousakka"
    case GingerTom(food)  => food
```

```scala
dinner(Lion)
// res2: String = "springbok"
dinner(GingerTom("lasagne"))
// res3: String = "lasagne"
```

So enums can model the same algebraic data types we build with sealed traits and case classes: parameterless cases correspond to case objects, and cases with parameters correspond to case classes.

### Under the Hood

Conceptually, Scala 3 enums are implemented using the same building blocks as traditional sealed algebraic data types. A simple enum like `Feline` behaves as if it were a sealed trait with a fixed set of case objects:

```scala
sealed trait Feline
object Feline:
  case object Lion  extends Feline
  case object Tiger extends Feline
```

While the compiler does not literally expand enums into this code, the runtime behaviour and pattern-matching semantics are equivalent.


### Take Home Points

- Scala 3 enums define a type and its cases together: `enum Feline: case Lion, Tiger`.
- Cases can be parameterless or have parameters: `case GingerTom(favouriteFood: String)`.
- Under the hood, enums use the same ideas as sealed traits and case classes or case objects; behaviour and exhaustiveness checking are equivalent.
- Pattern matching on enum values works like sealed traits; the compiler checks exhaustiveness.

