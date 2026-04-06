## Union Types

We have just seen how to model "this or that" data using sealed traits and final case classes. Scala 3 adds another way to express the same idea at the type level: *union types*. A union type `A | B` means a value that is either of type `A` or of type `B`. No extra trait or case classes are required when the alternatives are already existing types.

We write a union type using the `|` operator between two types:

```scala mdoc:silent
def describe(id: Int | String): String =
  id match
    case i: Int    => s"Integer: $i"
    case s: String => s"String: $s"
```

```scala mdoc
describe(42)
describe("hello")
```

The compiler treats `Int | String` as a single type. A value of that type can be an `Int` or a `String`, and we use pattern matching to discover which we have and to *narrow* the type in each branch. In the `case i: Int` branch, `i` is known to be `Int`; in the `case s: String` branch, `s` is known to be `String`.

<div class="callout callout-info">
#### Union Type Syntax {-}

A union type `A | B` denotes a value of type `A` or type `B`. Union types are a built-in way to express sum types when the alternatives are existing types.

```scala
def example(x: Int | String): Unit = x match
  case i: Int    => println(i + 1)
  case s: String => println(s.length)
```
</div>

### Pattern Matching on Union Types

We use the same `match` syntax we saw with case classes in the Pattern Matching section. The patterns we use for union types are *type patterns*: `case name: Type =>` matches when the value is of that type and binds it to the given name. The compiler then *narrows* the type in that branch, so we can safely use the value as that type (e.g. call `s.length` in the `String` case).

One case must cover each type in the union. For `Int | String` we need a case for `Int` and a case for `String`. Exhaustiveness checking for union types works like it does for sealed traits: the compiler will warn if we miss a case.

For example, here we match on a value of type `Int | String` and use type-specific operations in each branch:

```scala mdoc:silent
def doubleOrLength(x: Int | String): Int =
  x match
    case n: Int    => n * 2
    case s: String => s.length
```

```scala mdoc
doubleOrLength(21)
doubleOrLength("hello")
```

In the first branch `n` is narrowed to `Int`, so we can multiply; in the second branch `s` is narrowed to `String`, so we can call `.length`.

### When to Use Union Types

Union types and sealed traits both model "A or B". Choosing between them is mostly a matter of how the alternatives are defined:

- **Union types** fit when the alternatives are *existing* types: primitives (`Int`, `String`, `Boolean`), standard library types, or types you already have. They keep the code minimal and avoid introducing a new hierarchy.

- **Sealed traits** fit when you want to *define* the alternatives as part of the model: each case can carry different fields and methods, and the compiler can check that pattern matches are exhaustive. Use the sealed trait pattern when you are designing an algebraic data type with multiple variants.

For example, a function that accepts either an ID or a username might use a union type:

```scala mdoc:silent
def lookup(userId: Int | String): Option[String] =
  userId match
    case i: Int    => Some(s"user-$i")
    case s: String => Some(s)
```

For a richer model such as "a calculation that either succeeds with a result or fails with a message", the sealed trait and case class pattern is a better fit, because each case has different structure.

### Type Narrowing

After pattern matching on a union type, the compiler *narrows* the type in each branch. So you can safely call type-specific methods:

```scala mdoc:silent
def totalLength(x: Int | String): Int =
  x match
    case n: Int    => n
    case s: String => s.length
```

```scala mdoc
totalLength(10)
totalLength("hello")
```

Union types can have more than two alternatives: `A | B | C` is a value of type `A`, `B`, or `C`. Pattern matching handles each case in the same way.

### Take Home Points

- Union types (`A | B`) express "a value of type A or type B" without defining a new trait or classes.
- Use union types when the alternatives are existing types; use sealed traits when you are defining a new sum type with distinct cases and possibly different fields.
- Pattern matching on a union type both discriminates between cases and narrows the type in each branch.
