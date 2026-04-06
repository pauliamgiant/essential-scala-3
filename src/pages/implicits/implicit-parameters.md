## Using Parameters and Interfaces

We've seen the basics of the type class pattern. Now let's look at how we can make it easier to use. Recall our starting point is a trait `HtmlWriter` which allows us to implement HTML rendering for classes without requiring access to their source code, and allows us to render the same class in different ways.

```scala mdoc:invisible
case class Person(name: String, email: String)
```

```scala mdoc:silent
trait HtmlWriter[A]:
  def write(in: A): String

given personWriter: HtmlWriter[Person] with
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
```

This issue with this code is that we need manage a lot of `HtmlWriter` instances when we render any complex data. We have already seen that we can manage this complexity using given values and have mentioned *using parameters* in passing. In this section we go in depth on using parameters.

### Using Parameter Lists

Here is an example of a using parameter list:

```scala mdoc:silent
object HtmlUtil:
  def htmlify[A](data: A)(using writer: HtmlWriter[A]): String =
    writer.write(data)
```

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is a using parameter.

The `using` keyword applies to all parameters following it in the *same parameter group*. This makes the parameter list optional---when we call `HtmlUtil.htmlify` we can either specify the list as normal

```scala mdoc
HtmlUtil.htmlify(Person("John", "john@example.com"))(using personWriter)
```

For a method where we explicitly pass the given, we must use the using keyword as above.

or we can omit the using parameters. If we omit the using parameters, the compiler searches for given values of the correct type it can use to fill in the missing arguments. We have already learned about given values, but let's see a quick example to refresh our memory. First we define a given value.

```scala mdoc:silent
given ApproximationWriter: HtmlWriter[Int] with
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"
```

When we use `HtmlUtil` we don't have to specify the using parameter if a given value can be found.

```scala mdoc:silent
HtmlUtil.htmlify(2)
```

### Interfaces That Use Using Parameters

A complete use of the type class pattern requires an interface using using parameters, along with given type class instances. We've seen two examples already: the `sorted` method using `Ordering`, and the `htmlify` method above. The best interface depends on the problem being solved, but there is a pattern that occurs frequently enough that it is worth explaining here.

In many case the interface defined by the type class is the same interface we want to use. This is the case for `HtmlWriter` -- the only method of interest is `write`. We could write something like

```scala mdoc:silent
object HtmlWriter:
  def write[A](in: A)(using writer: HtmlWriter[A]): String =
    writer.write(in)
```

We can avoid this indirection (which becomes more painful to write as our interfaces become larger) with the following construction:

```scala mdoc:nest:silent
object HtmlWriter:
  def apply[A](using writer: HtmlWriter[A]): HtmlWriter[A] =
    writer
```

In use it looks like

```scala mdoc:invisible
given personWriter: HtmlWriter[Person] with
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
```

```scala mdoc:silent
HtmlWriter[Person].write(Person("Noel", "noel@example.org"))
```

The idea is to simply select a type class instance by type (done by the no-argument `apply` method) and then directly call the methods defined on that instance.

<div class="callout callout-info">
#### Type Class Interface Pattern {-}

If the desired interface to a type class `TypeClass` is exactly the methods defined on the type class trait, define an interface on the companion object using a no-argument `apply` method like

```scala mdoc:invisible
trait TypeClass[A]
```

```scala mdoc:silent
object TypeClass:
  def apply[A](using instance: TypeClass[A]): TypeClass[A] =
    instance
```
</div>

### Take Home Points

Using parameters make type classes more convenient to use. We can make an entire parameter list with the `using` keyword to make it a using parameter list.

```scala
def method[A](normalParam1: NormalType, ...)(using usingParam1: UsingType[A], ...)
```

If we call a method and do not explicitly supply its using parameter list, the compiler will search for given values of the correct types to complete the parameter list for us.

Using parameters we can make more convenient interfaces using type class instances. If the desired interface to a type class is exactly the methods defined on the type class we can create a convenient interface using the pattern

```scala mdoc:nest:silent
object TypeClass:
  def apply[A](using instance: TypeClass[A]): TypeClass[A] =
    instance
```

### Exercises

#### Equality Again

In the previous section we defined a trait `Equal` along with some implementations for `Person`.

```scala mdoc:nest:silent
case class Person(name: String, email: String)

trait Equal[A]:
  def equal(v1: A, v2: A): Boolean

given emailEqual: Equal[Person] with
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email

given nameEmailEqual: Equal[Person] with
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email && v1.name == v2.name
```

Implement an object called `Eq` with an `apply` method. This method should accept two explicit parameters of type `A` and a using `Equal[A]`. It should perform the equality checking using the provided `Equal`. With appropriate givens in scope, the following code should work

```scala
Eq(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
```

<div class="solution">
```scala mdoc:silent
object Eq:
  def apply[A](v1: A, v2: A)(using equal: Equal[A]): Boolean =
    equal.equal(v1, v2)
```
</div>

Package up the different `Equal` implementations as given values in their own objects, and show you can control the given selection by changing which object is imported.

In Scala 3, to import given instances (not regular values), you must use `import ObjectName.given` syntax. This makes it explicit that you're importing givens rather than regular values, keeping given scope management clear and safe.

<div class="solution">
```scala mdoc:reset-object:silent
trait Equal[A]:
  def equal(v1: A, v2: A): Boolean

case class Person(name: String, email: String)

object Eq:
  def apply[A](v1: A, v2: A)(using equal: Equal[A]): Boolean =
    equal.equal(v1, v2)

object NameAndEmailGiven:
  given Named: Equal[Person] with
    def equal(v1: Person, v2: Person): Boolean =
      v1.email == v2.email && v1.name == v2.name

object EmailGiven:
  given NamedEmail: Equal[Person] with
    def equal(v1: Person, v2: Person): Boolean =
      v1.email == v2.email

object Examples:
  def byNameAndEmail =
    import NameAndEmailGiven.given
    Eq(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))

  def byEmail =
    import EmailGiven.given
    Eq(Person("Noel", "noel@example.com"), Person("Dave", "noel@example.com"))
```
</div>

Now implement an interface on the companion object for `Equal` using the no-argument apply method pattern. The following code should work.

```
import NameAndEmailImplicit._
Equal[Person].equal(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
```

Which interface style do you prefer?

<div class="solution">
The following code is what we're looking for:

```scala mdoc:silent
object Equal:
  def apply[A](using instance: Equal[A]): Equal[A] =
    instance
```

In this case the `Eq` interface is slightly easier to use, as it requires less typing. For most complicated interfaces, with more than a single method, the companion object pattern would be preferred. In the next section we'll see how we can make interfaces that appear to be methods defined on the objects of interest.
</div>
