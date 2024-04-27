# scalaprops-magnolia

### build.sbt

for jvm

```scala
libraryDependencies += "com.github.scalaprops" %% "scalaprops-magnolia" % "0.7.0"
```

for scala-js

```scala
libraryDependencies += "com.github.scalaprops" %%% "scalaprops-magnolia" % "0.7.0"
```

### example

Import the content of `scalaprop.ScalapropsMagnolia` close to where you want `scalaprops.Gen` type classes to be automatically available for case classes / sealed hierarchies,

```scala
import scalaprops.{Gen, Cogen}
import scalaprops.ScalapropsMagnoliaGen._
import scalaprops.ScalapropsMagnoliaCogen._

sealed trait X 
case class Y(value: Boolean) extends X
case class Z(value: Int) extends X

Gen[X] // automatically available Gen[X] instance !
Cogen[X] // automatically available Cogen[X] instance !
```
