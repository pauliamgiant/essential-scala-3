## Variance

In this section we cover *variance annotations*, which allow us to control subclass relationships between types with type parameters. To motivate this, let's look again at our invariant generic sum type pattern.

Recall our `Maybe` type from the [Generics](#generics) section:

```scala mdoc:reset:silent
enum Maybe[A]:
  case Empty()
  case Full(value: A)
```

With this invariant definition, we must write `Empty[Int]()`, `Empty[String]()`, and so on—a fresh `Empty` for each element type. Ideally we would like a *single* `Empty` value that can serve as a `Maybe[Int]`, `Maybe[String]`, or `Maybe` of any type. We could try making `Empty` a parameterless case that extends `Maybe[Nothing]`:

```scala mdoc:reset:silent
enum Maybe[A]:
  case Full(value: A)
  case Empty extends Maybe[Nothing]
```

However, this leads to type errors:

```scala mdoc:fail
val possible: Maybe[Int] = Empty
```

The problem is that `Empty` is a `Maybe[Nothing]` and, with invariance, `Maybe[Nothing]` is not a subtype of `Maybe[Int]`. To overcome this we need variance annotations.


### Invariance, Covariance, and Contravariance

<div class="callout callout-info">
#### Variance is Hard {-}

Variance is one of the trickier aspects of Scala's type system. Although it is useful to be aware of its existence, we rarely have to use it in application code.
</div>

If we have some type `Foo[A]`, and `A` is a subtype of `B`, is `Foo[A]` a subtype of `Foo[B]`? The answer depends on the *variance* of the type `Foo`. The variance of a generic type determines how its supertype/subtype relationships change with respect with its type parameters:

A type `Foo[T]` is *invariant* in terms of `T`, meaning that the types `Foo[A]` and `Foo[B]` are unrelated regardless of the relationship between `A` and `B`. This is the default variance of any generic type in Scala.

A type `Foo[+T]` is *covariant* in terms of `T`, meaning that `Foo[A]` is a supertype of `Foo[B]` if `A` is a supertype of `B`. Most Scala collection classes are covariant in terms of their contents. We'll see these next chapter.

A type `Foo[-T]` is *contravariant* in terms of `T`, meaning that `Foo[A]` is a *subtype* of `Foo[B]` if `A` is a *supertype* of `B`. The only example of contravariance that I am aware of is function arguments.

### Function Types

When we discussed function types we glossed over how exactly they are implemented. Scala has 23 built-in generic classes for functions of 0 to 22 arguments. Here's what they look like:

```scala mdoc:reset:silent
trait Function0[+R]:
  def apply: R

trait Function1[-A, +B]:
  def apply(a: A): B

trait Function2[-A, -B, +C]:
  def apply(a: A, b: B): C

// and so on...
```

Functions are contravariant in terms of their arguments and covariant in terms of their return type. This seems counterintuitive but it makes sense if we look at it from the point of view of function arguments. Consider some code that expects a `Function1[A, B]`:

```scala mdoc:reset:silent
case class Box[A](value: A):
  /** Apply `func` to `value`, returning a `Box` of the result. */
  def map[B](func: Function1[A, B]): Box[B] =
    Box(func(value))
```

To understand variance, consider what functions can we safely pass to this `map` method:


 - A function from `A` to `B` is clearly ok.

 - A function from `A` to a subtype of `B` is ok because its result type will have all the properties of `B` that we might depend on. This indicates that functions are covariant in their result type.

 - A function expecting a supertype of `A` is also ok, because the `A` we have in the Box will have all the properties that the function expects.

 - A function expecting a subtype of `A` is not ok, because our value may in reality be a different subtype of `A`.


### Covariant Sum Types

Now we know about variance annotations we can solve our problem with `Maybe` by making it covariant:

```scala mdoc:reset:silent
enum Maybe[+A]:
  case Full(value: A)
  case Empty
```

The `+` before `A` makes `Maybe` covariant. The parameterless `Empty` case is inferred by the compiler as `Maybe[Nothing]`, and with covariance `Maybe[Nothing]` is a subtype of `Maybe[B]` for any `B`.

```scala mdoc
import Maybe.*
val perhaps: Maybe[Int] = Empty
```

It is easy to see how good looking and terse this pattern is compared to the invariant version.

This pattern is the most commonly used one with generic sum types. We should only use covariant types where the container type is immutable. If the container allows mutation we should only use invariant types.

<div class="callout callout-info">
#### Covariant Generic Sum Type Pattern {-}

If `A` of type `T` is a `B` or `C`, and `C` is not generic (has no type parameters), write

```scala mdoc:reset:silent
enum A[+T]:
  case B(t: T)
  case C
```

The parameterless case `C` is inferred as `A[Nothing]`, which is a subtype of `A[B]` for any `B` thanks to covariance. This pattern extends to more than one type parameter: if a type parameter is not needed for a specific case, the compiler substitutes `Nothing`.
</div>

```scala mdoc:reset:invisible
// clear previously defined types
```

### Contravariant Position

There is another pattern we need to learn for covariant sum types, which involves the interaction of covariant type parameters and contravariant method and function parameters. To illustrate this issue let's develop a covariant `Sum`.

#### Exercise: Covariant Sum

Implement a covariant `Sum` using the covariant generic sum type pattern.

<div class="solution">
```scala mdoc:reset:silent
enum Sum[+A, +B]:
  case Failure(value: A)
  case Success(value: B)
```
</div>

Now let's see what happens when we implement `flatMap` on `Sum`.

#### Exercise: Some sort of flatMap

Implement `flatMap` and verify you receive an error like

```scala
error: covariant type A occurs in contravariant position in type B => Sum[A,C] of value f
  def flatMap[C](f: B => Sum[A, C]): Sum[A, C] =
                 ^
```

<div class="solution">
```scala mdoc:fail:silent
object wrapper:
  enum Sum[+A, +B]:
    case Failure(value: A)
    case Success(value: B)
    def flatMap[C](f: B => Sum[A, C]): Sum[A, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => f(v)

import wrapper.*
import wrapper.Sum.*
```
</div>

What is going on here? Let's momentarily switch to a simpler example that illustrates the problem.

```scala mdoc:fail:silent
case class Box[+A](value: A):
  def set(a: A): Box[A] = Box(a)
```

which causes the error

```scala
error: covariant type A occurs in contravariant position in type A of value a
  def set(a: A): Box[A] = Box(a)
          ^
```

Remember that functions, and hence methods, which are just like functions, are contravariant in their input parameters. In this case we have specified that `A` is covariant but in `set` we have a parameter of type `A` and the type rules requires `A` to be contravariant here. This is what the compiler means by a "contravariant position".

The solution is introduce a new type that is a supertype of `A`. We can do this with the notation `[AA >: A]` like so:

```scala mdoc:reset:silent
case class Box[+A](value: A):
  def set[AA >: A](a: AA): Box[AA] = Box(a)
```

This successfully compiles.

Back to `flatMap`, the function `f` is a parameter, and thus in a contravariant position. This means we accept *supertypes* of `f`. It is declared with type `B => Sum[A, C]` and thus a supertype is *covariant* in `B` and *contravariant* in `A` and `C`. `B` is declared as covariant, so that is fine. `C` is invariant, so that is fine as well. `A` on the other hand is covariant but in a contravariant position. Thus we have to apply the same solution we did for `Box` above.

```scala mdoc:reset:silent
object wrapper:
  enum Sum[+A, +B]:
    case Failure(value: A)
    case Success(value: B)
    def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => f(v)

import wrapper.*
import wrapper.Sum.*
```

<div class="callout callout-info">
#### Contravariant Position Pattern {-}

If a covariant type parameter `T` appears in a contravariant position (e.g. a method parameter) and the compiler complains, introduce a new type parameter with a lower bound: `TT >: T`.

```scala mdoc:reset:silent
enum A[+T]:
  case B
  def f[TT >: T](t: TT): A[TT] = ???
```
</div>


### Type Bounds

```scala mdoc:reset

```

We have seen some type bounds above, in the contravariant position pattern. Type bounds extend to specify subtypes as well as supertypes. The syntax is `A <: Type` to declare `A` must be a subtype of `Type` and `A >: Type` to declare a supertype.

For example, the following type allows us to store a `Visitor` or any subtype:

```scala mdoc:reset:invisible
trait Visitor
```

```scala mdoc:silent
case class WebAnalytics[A <: Visitor](
  visitor: A,
  pageViews: Int,
  searchTerms: List[String],
  isOrganic: Boolean,
)
```


### Exercises

#### Covariance and Contravariance

```scala mdoc:invisible
object catExample:
  trait Animal
  trait Cat extends Animal:
    val color: String; val food: String
  object Cat:
    def apply(aColor: String, aFood: String) =
      new Cat:
        val color = aColor; val food = aFood
  trait Siamese extends Cat

  trait Sound
  trait CatSound extends Sound
  trait Purr extends Sound
import catExample.*
```

Using the notation `A <: B` to indicate `A` is a subtype of `B` and assuming:

- `Siamese <: Cat <: Animal`; and
- `Purr <: CatSound <: Sound`

if I have a method

```scala mdoc:silent
def groom(groomer: Cat => CatSound): CatSound =
  val oswald = Cat("Black", "Cat food")
  groomer(oswald)
```

which of the following can I pass to `groom`?

- A function of type `Animal => Purr`
- A function of type `Siamese => Purr`
- A function of type `Animal => Sound`

<div class="solution">
The only function that will work is the the function of type `Animal => Purr`. The `Siamese => Purr` function will not work because the Oswald is a not a Siamese cat. The `Animal => Sound` function will not work because we require the return type to be a `CatSound`.
</div>


#### Calculator Again

We're going to return to the interpreter example we saw at the end of the last chapter. This time we're going to use the general abstractions we've created in this chapter, and our new knowledge of `map`, `flatMap`, and `fold`.

We're going to represent calculations as `Sum[String, Double]`, where the `String` is an error message. Extend `Sum` to have `map` and `fold` method.

<div class="solution">
```scala mdoc:reset:silent
object wrapper:
  enum Sum[+A, +B]:
    case Failure(value: A)
    case Success(value: B)
    def fold[C](error: A => C, success: B => C): C =
      this match
        case Failure(v) => error(v)
        case Success(v) => success(v)

    def map[C](f: B => C): Sum[A, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => Success(f(v))

    def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => f(v)

import wrapper.*
import wrapper.Sum.*
```
</div>

Now we're going to reimplement the calculator from last time. We have an abstract syntax tree defined via the following algebraic data type:

```scala mdoc:reset:silent
enum Expression:
  case Addition(left: Expression, right: Expression)
  case Subtraction(left: Expression, right: Expression)
  case Division(left: Expression, right: Expression)
  case SquareRoot(value: Expression)
  case Number(value: Double)
```

Now implement a method `eval: Sum[String, Double]` on `Expression`. Use `flatMap` and `map` on `Sum` and introduce any utility methods you see fit to make the code more compact. Here are some test cases:

```scala
import Expression._
import wrapper.Sum._
assert(Addition(Number(1), Number(2)).eval == Success(3))
assert(SquareRoot(Number(-1)).eval == Failure("Square root of negative number"))
assert(Division(Number(4), Number(0)).eval == Failure("Division by zero"))
assert(Division(Addition(Subtraction(Number(8), Number(6)), Number(2)), Number(2)).eval == Success(2.0))
```

<div class="solution">
Here's my solution. I used a helper method `lift2` to "lift" a function into the result of two expressions. I hope you'll agree the code is both more compact and easier to read than our previous solution!

```scala mdoc:invisible
object wrapper:
  enum Sum[+A, +B]:
    case Failure(value: A)
    case Success(value: B)
    def fold[C](error: A => C, success: B => C): C =
      this match
        case Failure(v) => error(v)
        case Success(v) => success(v)

    def map[C](f: B => C): Sum[A, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => Success(f(v))

    def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
      this match
        case Failure(v) => Failure(v)
        case Success(v) => f(v)

import wrapper.*
import wrapper.Sum.*
```

```scala mdoc:nest:silent
import wrapper.Sum.*

enum Expression:
  case Addition(left: Expression, right: Expression)
  case Subtraction(left: Expression, right: Expression)
  case Division(left: Expression, right: Expression)
  case SquareRoot(value: Expression)
  case Number(value: Double)

  def eval: Sum[String, Double] =
    this match
      case Addition(l, r)    => lift2(l, r, (left, right) => Success(left + right))
      case Subtraction(l, r) => lift2(l, r, (left, right) => Success(left - right))
      case Division(l, r)    => lift2(
          l,
          r,
          (left, right) =>
            if right == 0 then
              Failure("Division by zero")
            else
              Success(left / right),
        )
      case SquareRoot(v) =>
        v.eval.flatMap { value =>
          if value < 0 then
            Failure("Square root of negative number")
          else
            Success(Math.sqrt(value))
        }
      case Number(v) => Success(v)

  def lift2(l: Expression, r: Expression, f: (Double, Double) => Sum[String, Double]): Sum[String, Double] =
    l.eval.flatMap { left =>
      r.eval.flatMap { right =>
        f(left, right)
      }
    }
```
</div>
