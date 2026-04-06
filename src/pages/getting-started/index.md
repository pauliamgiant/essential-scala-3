# Getting Started

Throughout this book we will be working with short examples of Scala code. There are two recommended ways of doing this:

 1. Using the *Scala console* (better for people who like command lines)

 2. Using *Worksheets* in *VS Code with Metals* (better for people who like IDEs)

We'll walk through the setup for each process here.

## Setting up the Scala Console

Follow the instructions on [http://scala-lang.org](https://docs.scala-lang.org/getting-started/install-scala.html) to set Scala up on your computer. Once Scala is installed, you should be able to run an interactive console by typing `scala` at your command line prompt. Here's an example from OS X:

```zsh
dave@computer ~> scala
Welcome to Scala 3.7.4 (21, Java OpenJDK 64-Bit Server VM).
Type in expressions for evaluation. Or try :help.

scala>
```

You can enter individual expressions at the `scala>` prompt and press *Enter* to compile and execute them:

```scala mdoc
"Hello world!"
```

### Entering Single-Line Expressions

Let's try entering a simple expression:

```scala mdoc
1 + 2 + 3
```

When we press Enter, the console responds with three things:

 - an *identifier* `res1`;
 - a *type* `Int`;
 - a *value* `6`.

As we will see in the next chapter, every expression in Scala has a *type* and a *value*. The type is determined at compile time and the value is determined by executing the expression. Both of these are reported here.

The identifier `res1` is a convenience provided by the console to allow us to refer to the result of the expression in future expressions. For example, we can multiply our result by two as follows:

```scala mdoc
res1 * 2
```

If we enter an expression that doesn't yield a useful value, the console won't print anything in response:

```scala mdoc
println("Hello world!")
```

Here, the output `"Hello world!"` is from our `println` statement---the expression we entered doesn't actually return a value. The console doesn't provide output similar to the output we saw above.

### Entering Multi-Line Expressions

We can split long expressions across multiple lines quite simply. If we press enter before the end of an expression, the console will print a `|` character to indicate that we can continue on the next line:

```scala
for(i <- 1 to 3) {
| println(i)
| }

```

You can simply continue typing across multiple lines, and the REPL will intelligently detect when your expression is complete.

There are two ways we can enter this multi-line expression. First is to use braces to create a block expression:

```scala
scala> val result = {
     |   val x = 1
     |   val y = 2
     |   x + y
     | }
val result: Int = 3
```

The second way is to use an indentation of 2 spaces which is the preferred way in Scala 3:

```scala
scala> val result =
     |   val x = 1
     |   val y = 2
     |   x + y
     |
val result: Int = 3
```

If you have Scala code in a file, you can use the `:load` command to load the contents of the file into the console. This is much more convenient than re-entering expressions. 

For example, with a file named `example.scala` containing `1 + 2 + 3` we can use `:load` to load the file into the console.

Let's first create the file.

To create a file very easily, in a separate terminal window outside of the Scala REPL, you can use the `touch` command on Mac or Linux, or the New-Item command in Windows PowerShell

OSX
```bash
touch example.scala
```

Powershell
```powershell
New-Item example.scala

```

This will create a new file called `example.scala` in the current directory.
You can then open the file in any text editor and add the code below. 
Examples of editors are 'nano' or 'vim' on Mac and Linux or Notepad on windows.


We'll use 'nano' on Mac in this example.

```bash
nano example.scala
```

This will open the file in the nano text editor. You can then paste the code below.

```scala
val x = 1
val y = 2
println("Example file evaluates and prints:")
println(x + y)
```

to save the file, press `Ctrl+O` and then press `Enter`. To exit the editor, press `Ctrl+X`.

Now, back in the Scala REPL, you can load the file into the console using the `:load` command:

```scala
scala> :load example.scala
"Example file evaluates and prints:"
3
val x: Int = 1
val y: Int = 2"
```

### Printing the Type of an Expression

One final tip for using the console. Occasionally we want to know the *type* of an expression without actually running it. To do this we can use the `:type` command:

```scala
scala> :type println("Hello world!")
Unit
```

Notice that the console doesn't execute our `println` statement in this expression. It simply compiles it and prints out its type, which in this case is something called `Unit`.

`Unit` is Scala's equivalent of `void` from Java and C. Read Chapter 1 to find out more.

## Setting up VS Code with Metals

*Metals* is the Scala language server that provides IDE features for Scala 3. It works with several editors, and we recommend using it with *Visual Studio Code* (VS Code), which is a free, open-source editor with excellent Scala support.

### Installing VS Code

If you don't already have VS Code installed:

1. Go to [https://code.visualstudio.com](https://code.visualstudio.com)
2. Download the installer for your operating system
3. Run the installer and follow the on-screen instructions
4. Launch VS Code

### Installing the Metals Extension

Once you have VS Code installed, you need to install the Metals extension:

1. Open VS Code
2. Click on the Extensions icon in the left sidebar (or press `Ctrl+Shift+X` / `Cmd+Shift+X` on Mac)
3. Search for "Scala (Metals)"
4. Click **Install** on the "Scala (Metals)" extension by Scalameta
5. Wait for the installation to complete

Metals will automatically download and configure the Scala tooling when you open a Scala project.

### Creating your First Application

Now let's create your first Scala project. We'll use sbt (the Scala Build Tool) to create a new project:

1. Open a terminal/command prompt - you can use the terminal in VS Code (Mac: cmd+j, Windows: ctrl+j) or a separate terminal window
2. Navigate to the directory where you want to create your project
3. Run the following command:

```bash
sbt new scala/scala3.g8
```

4. When prompted, enter a name for your project (e.g., `essential-scala`)
5. Open the project folder in VS Code: **File > Open Folder**

When you open the Scala project, Metals will automatically:

- Detect the project
- Import the build
- Index your code
- Enable IDE features like syntax highlighting, code completion, and error checking

If a message pops up saying *"New sbt workspace detected, would you like to import the build?"*, click **Import Build**.

### Your First Scala Application

The Scala 3 template creates a simple "Hello World" application for you. In your project, navigate to `src/main/scala` and you'll find a file called `Main.scala` with the following content:

```scala
@main def hello(): Unit =
  println("Hello world!")
  println(msg)

def msg = "I was compiled by Scala 3. :)"
```

Notice the `@main` annotation - this is the Scala 3 way to define a program entry point. 

To run your application, open the terminal in VS Code and run:

```bash
sbt run
```

You should see the output:

```
[info] running hello 
Hello world!
I was compiled by Scala 3. :)
[success] Total time: 1 s, completed 3 Feb 2026, 06:12:41
```

Congratulations - you just ran your first Scala application!


Developers with Java experience will notice that Scala 3's `@main` annotation is much simpler than the traditional Java hello world app:

```java
public class HelloWorld {
  public static void main(String[] args) {
    System.out.println("Hello world!");
  }
}
```

The resemblance is, of course, no coincidence. These two applications compile to more or less the same bytecode and have exactly the same semantics. We will learn more about the similarities and differences between Scala and Java as the course continues.

### Creating your First Worksheet

Compiling and running code whenever you make a change is a time consuming process that isn't particularly suitable to a learning environment.

Fortunately, Metals supports special files called *Scala Worksheets* that are specifically designed for training and experimentation. Worksheets allow you to evaluate Scala expressions and see results inline, providing instant feedback which is exactly what we need when investigating new concepts!

To create your first Scala Worksheet:

1. In VS Code, navigate to the `src/main/scala` directory in your project
2. Right click on the `Main.scala` file and select "New Scala file"
3. Select "Worksheet" and enter a name for the worksheet, e.g. "FirstSteps"
4. VS Code will recognize this as a Scala worksheet and create a new file with the `.worksheet.sc` extension

Metals will automatically evaluate your worksheet. Once you save the file, you'll see the results displayed as decorations in the editor, showing the value and type of each expression.

The results will appear inline, showing something like:

```scala
println("Welcome to the Scala worksheet")   // Welcome to the Scala worksheet

1 + 1                                       // val res0: Int = 2

if 20 > 10 then "left" else "right"         // val res1: String = "left"

println("The ultimate answer is " + 42)     // The ultimate answer is 42
```

We'll dive into what all of this output means as we proceed with the course ahead. For now you're all set to start honing your Scala skills!
