## Using Type Classes

We have seen how to define type classes. In this section we'll see some conveniences for using them: *context bounds* and the *summon* method.

### Context Bounds

When we use type classes we often end up requiring using parameters that we pass onward to a type class interface. For example, using our `HtmlWriter` example we might want to define some kind of page template that accepts content rendered by a writer.

```scala mdoc:invisible
trait HtmlWriter[A]:
  def write(in: A): String

extension [A](value: A)
  def toHtml(using writer: HtmlWriter[A]) =
    writer.write(value)
```

```scala mdoc:silent
def pageTemplate[A](body: A)(using writer: HtmlWriter[A]): String =
  val renderedBody = body.toHtml

  s"<html><head>...</head><body>${renderedBody}</body></html>"
```

We don't explicitly use the using parameter `writer` in our code, but we need it in scope so the compiler can insert it for the `toHtml` extension.

Context bounds allow us to write this more compactly, with a notation that is reminiscent of a type bound.

```scala mdoc:nest:silent
def pageTemplate[A: HtmlWriter](body: A): String =
  val renderedBody = body.toHtml

  s"<html><head>...</head><body>${renderedBody}</body></html>"
```

The context bound is the notation `[A : HtmlWriter]` and it expands into the equivalent using parameter list in the prior example.

<div class="callout callout-info">
#### Context Bound Syntax {-}

A context bound is an annotation on a generic type variable like so:

```scala
[A : Context]
```

It expands into a generic type parameter `[A]` along with a using parameter for a `Context[A]`.
</div>

### Summon

Context bounds give us a short-hand syntax for declaring using parameters, but since we don't have an explicit name for the parameter we cannot use it in our methods. Normally we use context bounds when we don't need explicit access to the using parameter, but rather just pass it on to some other method. However if we do need access for some reason we can use the `summon` method.

```scala mdoc:silent
case class Example(name: String)
given exampleGiven: Example = Example("given")
```

```scala mdoc
summon[Example]

summon[Example] == exampleGiven
```

The `summon` method takes no parameters but has a generic type parameter. It returns the given matching the given type, assuming there is no ambiguity.
