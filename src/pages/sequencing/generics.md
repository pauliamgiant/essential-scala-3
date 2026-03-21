## Generics

Generic types allow us to *abstract over types*. There are useful for all sorts of data structures, but commonly encountered in collections so that's where we'll start.

### Pandora's Box

Let's start with a collection that is even simpler than our list---a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

```scala mdoc:silent
final case class Box[A](value: A)
```

```scala mdoc
Box(2)

res0.value

Box("hi") // if we omit the type parameter, scala will infer its value

res2.value
```

The syntax `[A]` is called a *type parameter*. We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

```scala mdoc:silent
def generic[A](in: A): A = in
```

```scala mdoc
generic[String]("foo")

generic(1) // again, if we omit the type parameter, scala will infer it
```

Type parameters work in a way analogous to method parameters. When we call a method we bind the method's parameter names to the values given in the method call. For example, when we call `generic(1)` the name `in` is bound to the value `1` within the body of `generic`.

When we call a method or construct a class with a type parameter, the type parameter is bound to the concrete type within the method or class body. So when we call `generic(1)` the type parameter `A` is bound to `Int` in the body of `generic`.

<div class="callout callout-info">
#### Type Parameter Syntax {-}

We declare generic types with a list of type names within square brackets like `[A, B, C]`. By convention we use single uppercase letters for generic types.

Generic types can be declared in a class or trait declaration in which case they are visible throughout the rest of the declaration.

```scala
case class Name[A](...): ... 
trait Name[A]: ... 
enum Name[A]: ...
```

Alternatively they may be declared in a method declaration, in which case they are only visible within the method.

```scala
def name[A](...){ ... }
```
</div>

### Generic Algebraic Data Types

We described type parameters as analogous to method parameters, and this analogy continues when extending a trait that has type parameters. Extending a trait, as we do in a sum type, is the type level equivalent of calling a method and we must supply values for any type parameters of the trait we're extending.

In previous sections we've seen sum types like the following:

```scala mdoc:reset:silent
enum Calculation:
  case Success(result: Double)
  case Failure(reason: String)
```

Let's generalise this so that our result is not restricted to a `Double` but can be some generic type. In doing so let's change the name from `Calculation` to `Result` as we're not restricted to numeric calculations anymore. Now our data definition becomes:

A `Result` of type `A` is either a `Success` of type `A` or a `Failure` with a `String` reason. This translates to the following code:

```scala mdoc:reset:silent
enum Result[A]:
  case Success(result: A)
  case Failure(reason: String)
```

`Success` carries a value of type `A`, while `Failure` carries only a `String` reason and does not use `A`. The `+` (covariance) is needed so `Failure` can serve as a `Result[B]` for any type `B`; we explore variance in the [Variance](variance.html) section.

<div class="callout callout-info">
#### Generic Sum Type Pattern {-}

If `A` of type `T` is a `B` or `C` write

```scala mdoc:reset:silent
enum A[T]:
  case B()
  case C()
```
</div>

```scala mdoc:reset:invisible
// clear the types defined so far
```

### Exercises

#### Generic List

Our `IntList` type was defined as

```scala mdoc:reset:silent
enum IntList:
  case End
  case Pair(head: Int, tail: IntList)
```

Change the name to `LinkedList` and make it generic in the type of data stored in the list.

<div class="solution">

This is an application of the generic sum type pattern.

```scala mdoc:reset:silent
enum LinkedList[A]:
  case Empty()
  case Pair(head: A, tail: LinkedList[A])
```
</div>

#### Working With Generic Types

There isn't much we can do with our `LinkedList` type. Remember that types define the available operations, and with a generic type like `A` there isn't a concrete type to define any available operations. (Generic types are made concrete when a class is instantiated, which is too late to make use of the information in the definition of the class.)

However, we can still do some useful things with our `LinkedList`! Implement `length`, returning the length of the `LinkedList`. Some test cases are below.

```scala
val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example.length == 3)
assert(example.tail.length == 2)
assert(Empty().length == 0)
```

<div class="solution">
This code is largely unchanged from the implementation of `length` on `IntList`.

```scala mdoc:reset:silent
object wrapper:
  enum LinkedList[A]:
    case Empty()
    case Pair(head: A, tail: LinkedList[A])
    def length: Int =
      this match
        case Pair(hd, tl) => 1 + tl.length
        case Empty()      => 0

import wrapper.*
import wrapper.LinkedList.*
```
</div>

On the JVM we can compare all values for equality. Implement a method `contains` that determines whether or not a given item is in the list. Ensure your code works with the following test cases:

```scala
val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example.contains(3) == true)
assert(example.contains(4) == false)
assert(Empty().contains(0) == false)
// This should not compile
// example.contains("not an Int")
```

<div class="solution">
This is another example of the standard structural recursion pattern.

```scala mdoc:reset:silent
object wrapper:
  enum LinkedList[A]:
    case Empty()
    case Pair(head: A, tail: LinkedList[A])
    def contains(item: A): Boolean =
      this match
        case Pair(hd, tl) =>
          if hd == item
          then true
          else tl.contains(item)
        case Empty() => false

import wrapper.*
import wrapper.LinkedList.*
```
</div>

Implement a method `apply` that returns the <em>n<sup>th</sup></em> item in the list

**Hint:** If you need to signal an error in your code (there's one situation in which you will need to do this), consider throwing an exception. Here is an example:

```scala mdoc:fail:silent
throw Exception("Bad things happened")
```

Ensure your solution works with the following test cases:

```scala
val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example(0) == 1)
assert(example(1) == 2)
assert(example(2) == 3)
assert(try {
  example(3)
  false
} catch {
  case e: Exception => true
})
```

<div class="solution">
There are a few interesting things in this exercise. Possibly the easiest part is the use of the generic type as the return type of the `apply` method.

Next up is the `End` case, which the hint suggested you through an `Exception` for. Strictly speaking we should throw Java's `IndexOutOfBoundsException` in this instance, but we will shortly see a way to remove exception handling from our code altogether.

Finally we get to the actual structural recursion, which is perhaps the trickiest part. The key insight is that if the index is zero, we're selecting the current element, otherwise we subtract one from the index and recurse. We can recursively define the integers in terms of addition by one. For example, 3 = 2 + 1 = 1 + 1 + 1. Here we are performing structural recursion on the list *and* on the integers.

```scala mdoc:reset:silent
object wrapper:
  enum LinkedList[A]:
    case Empty()
    case Pair(head: A, tail: LinkedList[A])
    def apply(index: Int): A =
      this match
        case Pair(hd, tl) =>
          if index == 0
          then hd
          else tl(index - 1)
        case Empty() =>
          throw Exception("Attempted to get element from an Empty list")

import wrapper.*
import wrapper.LinkedList.*
```
</div>

Throwing an exception isn't cool. Whenever we throw an exception we lose type safety as there is nothing in the type system that will remind us to deal with the error. It would be much better to return some kind of result that encodes we can succeed or failure. We introduced such a type in this very section.

```scala mdoc:reset:silent
enum Result[A]:
  case Success(result: A)
  case Failure(reason: String)
```

Change `apply` so it returns a `Result`, with a failure case indicating what went wrong. Here are some test cases to help you:

```scala
assert(example(0) == Success(1))
assert(example(1) == Success(2))
assert(example(2) == Success(3))
assert(example(3) == Failure("Index out of bounds"))
```

<div class="solution">
```scala
object wrapper:
  enum Result[A]:
    case Success(result: A)
    case Failure(reason: String)

  enum LinkedList[A]:
    case Empty()
    case Pair(head: A, tail: LinkedList[A])
    def apply(index: Int): Result[A] =
      this match
        case Pair(hd, tl) =>
          if index == 0 then
            Success(hd)
          else
            tl(index - 1)
        case Empty() =>
          Failure("Index out of bounds")

import wrapper._
import wrapper.LinkedList._
import wrapper.Result._
```
</div>
