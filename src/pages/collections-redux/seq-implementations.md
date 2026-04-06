## Sequence Implementations

We've seen that the Scala collections separate interface from implementation. This means we can work with all collections in a generic manner. However different concrete implementations have different performance characteristics, so we must be aware of the available implementations so we can choose appropriately. Here we look at the most frequently used implementations of `Seq`. For full details on all the available implementations, see [the docs](http://docs.scala-lang.org/overviews/collections/introduction.html).

### Performance Characteristics

The collections framework distinguishes at the type level two general classes of sequences. Sequences implementing `IndexedSeq` have efficient `apply`, `length`, and (if mutable) `update` operations, while `LinearSeq`s have efficient `head` and `tail` operations. Neither have any additional operations over `Seq`.

### Immutable Implementations

The main immutable `Seq` implementations are `List`, `LazyList`, and `Vector`.

#### List

A `List` is a singly linked list. It has constant time access to the first element and remainder of the list (`head`, and `tail`) and is thus a `LinearSeq`. It also has constant time prepending to the front of the list, but linear time appending to the end. `List` is the default `Seq` implementation.

#### LazyList

A `LazyList` is like a list except its elements are computed on demand, and thus it can have infinite size. Unlike the old `Stream` (deprecated since Scala 2.13), `LazyList` is fully lazy---both head and tail are evaluated only when needed. Like other collections we can create lazy lists by calling the `apply` method on the companion object.

```scala mdoc
LazyList(1, 2, 3)
```

Note that the elements are not evaluated until we access them.

We can also use the `#::` method to construct a lazy list from individual elements, starting from `LazyList.empty`.

```scala mdoc
LazyList.empty.#::(3).#::(2).#::(1)
```

We can also use the more natural operator syntax.

```scala mdoc
1 #:: 2 #:: 3 #:: LazyList.empty
```

This method allows us to create an infinite lazy list. Here's an infinite lazy list of 1s:

```scala mdoc:silent
def ones: LazyList[Int] = 1 #:: ones
```

```scala mdoc
ones
```

Because elements are only evaluated as requested, calling `ones` doesn't lead to infinite recursion. When we take the first five elements (and convert them to a `List`, so they'll all print out) we see we have what we want.

```scala mdoc
ones.take(5).toList
```

#### Vector

`Vector` is the final immutable sequence we'll consider. Unlike `LazyList` and `List` it is an `IndexedSeq`, and thus offers fast random access and updates. It is the default immutable `IndexedSeq`, which we can see if we create one.

```scala mdoc
scala.collection.immutable.IndexedSeq(1, 2, 3)
```

Vectors are a good choice if you want both random access and immutability.


### Mutable Implementations

The mutable collections are probably more familiar. In addition to linked lists and arrays (which we discuss in more detail later) there are buffers, which allow for efficient construction of certain data structures.

#### Buffers

`Buffer`s are used when you want to efficiently create a data structure an item at a time. An `ArrayBuffer` is an `IndexedSeq` which also has constant time appends. A `ListBuffer` is like a `List` with constant time prepend *and* append (though note it is mutable, unlike `List`).

Buffers' add methods to support destructive prepends and appends. For example, the `+=` is destructive append.

```scala mdoc
val buffer = new scala.collection.mutable.ArrayBuffer[Int]()

buffer += 1

buffer
```


#### StringBuilder

A `StringBuilder` is essentially a buffer for building strings. It is mostly the same as Java's `StringBuilder` except that it implements standard Scala collections method where there is a conflict. So, for example, the `reverse` method creates a new `StringBuilder` unlike in Java.

#### LinkedLists

Mutable singly `LinkedList`s and `DoubleLinkedList`s work for the most part just like `List`. A `DoubleLikeList` maintains both a `prev` and `next` pointer and so allows for efficient removal of an element.
