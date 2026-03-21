---
layout: page
title: Given Resolution
---

## Given Resolution Rules

Scala has several features involving givens---given values, extension methods, and conversions using givens. Each works in the same way---the compiler detects a type error in our code, locates a matching given, and applies it to fix the error. This is a powerful mechanism, but we need to control it very carefully to prevent the compiler changing our code in ways we don't expect. For this reason, there is a strict set of **given resolution rules** that we can use to dictate the compiler's behaviour:

 1. **Explicits first rule**---if the code already type checks, the compiler ignores givens altogether;
 2. **Marking rule**---the compiler only uses definitions marked with the `given` keyword;
 3. **Scope rule**---the compiler only uses definitions that are *in scope* at the current location in the code (see below);
 4. **Non-ambiguity rule**---the compiler only applies a given if it is the only candidate available;
 5. **One-at-a-time rule**---the compiler never chains givens together to fix type errors---doing so would drastically increase compile times;

Note that the name of the given doesn't come into play in this process.

### Given Scope

The *scope rule* of given resolution uses a special set of scoping rules that allow us to package givens in useful ways. These rules, collectively referred to as **given scope**, form a search path that the compiler uses to locate givens:

 1. **Local scope**---First look locally for any identifier that is tagged as `given`. This must be a single identifier (i.e. `a`, not `a.b`), and can be defined locally or in the surrounding class, object, or trait, or `imported` from elsewhere.

 2. **Companion objects**---If a given cannot be found locally, the compiler looks in the companion objects of types involved in the type error. Will see more of this rule in the next section.

## Packaging Given Values

Givens **can be defined at the top level** in Scala 3. They can also be wrapped in an outer trait, class, or singleton object for organization. A common pattern for packaging a given value is to define it inside a trait and then bring instances into scope through imports:

```scala mdoc:silent
val vowels = Seq('a', 'e', 'i', 'o', 'u')

trait VowelExtensions:
  extension (str: String)
    def numberOfVowels =
      str.toList.filter(vowels.contains(_)).length

object VowelExtensions extends VowelExtensions
```

This gives developers two convenient ways of using our code:

 1. quickly bring our extension into scope via the object using an `import`:

    ```scala mdoc:silent
    // `numberOfVowels` is not in scope here

    def testMethod =
      import VowelExtensions.*

      // `numberOfVowels` is in scope here

      "the quick brown fox".numberOfVowels

    // `numberOfVowels` is no longer in scope here
    ```

 2. stack our extansion definitions with a set of other givens to produce a library of Extensions that can be brought into scope using inheritance or an `import`:

    ```scala mdoc:invisible
    trait VowelExtensions
    trait MoreExtensions
    trait YetMoreExtensions
    ```

    ```scala mdoc:silent
    object AllTheExtensions extends VowelExtensions with MoreExtensions with YetMoreExtensions

    import AllTheExtensions.*

    // Extensions are in scope here
    // along with other extension definitions
    ```

<div class="alert alert-info">
**Givens tip:** Some Scala developers have concerns about using givens because they can be hard to debug. The reason for this is that a given definition at one point in our codebase can have an invisible affect on the meaning of a line of code written elsewhere.

While this is a valid criticism, the solution is not to abandon them altogether but to apply strict design principles to regulate their use. Here are some tips:

 1. Keep tight control over the scope of your givens. Import them only where you want to use them.

 2. Package all your givens in traits/objects with organized names. This makes them easy to find using a global search across your codebase.

 3. Only use givens on specific types. Defining a given extension on a general type like `Any` is more likely to cause problems than defining it on a specific type like `WebSiteVisitor`.

The same resolution rules apply for given values as for extensions. If the compiler is unable to find suitable candidates for all using parameters, we get a compilation error.

Let's redefine our adapters for `HtmlWriter` so we can bring them all into scope. Given values can be defined at the top level or inside other classes, objects, or traits:

```scala mdoc:invisible
trait HtmlWriter[A]:
  def write(a: A): String

case class Person(name: String, email: String)
import java.util.Date
```

```scala mdoc:silent
object HtmlImplicits:
  given PersonWriter: HtmlWriter[Person] with
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"

  given DateWriter: HtmlWriter[Date] with
    def write(in: Date) = s"<span>${in.toString}</span>"
```

```scala mdoc:invisible
object HtmlUtil:
  def htmlify[A](data: A)(using writer: HtmlWriter[A]): String =
    writer.write(data)
```

We can now use our adapters with `htmlify`:

```scala mdoc
import HtmlImplicits.given

HtmlUtil.htmlify(Person("John", "john@example.com"))
```

This version of the code has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlImplicits` implement the functionality as a set of adapters, and the using parameter to `htmlify` automatically selects the correct adapter for any given argument. However, we can take things one step further to really simplify things.
