# scala-oriented

**_scala-oriented_** is a Scala wrapper for the OrientDB Java API, _oriented_ on functional and typeful programming. The library uses the extended SQL queries and commands from the Java API,  encapsulating side-effect and queries resulting in meaningful types.  

### Not ready for use (YET!)

## Quickstart

Releases and dependencies are shown below.

| scala-oriented |  status  | JDK  | scala  | Graph-DB | Server |
| :------------: | :------: | :--: | :----: | :------: | :----: |
|     0.0.1      | unstable | 1.8  | 2.11.8 |  2.2.7   | 2.2.7  |

_Note that scala-orient is still in very early development and will undergo many changes._

To use scale-oriented add the following library dependency to your `build.sbt`.

```scala
// TODO Externalize Library!
libraryDependencies += "todo.todo" %% "scala-oriented" % "0.0.1"
```

Two imports are needed to use scala-orient in scala files (the rest of this readme assumes that these imports are in scope).

```scala
import oriented._     // Imports all required types
import oriented.sql._ // Imports Extended SQL interporlation
```

## Common Use Cases

The next sections will show code examples of common use cases using the scale-oriented library. This is intended to show how the library should be used. Use cases can depend on each other, where you have to create a Vertex Class before being able to insert new vertices. The following simple case classes need to be defined to follow along with the examples.

```scala
// Data Class that represents an user will be saved as a Vertex
case class User(name: String, age: Int, address: String)
// Data Class that represents an order will be saved as a Vertex
case class Product(name: String, price: Double)
// Data class that represents the edge from user to product
case class Ordered(orderId: String, amount: Int, date: String)
```

Here a User represents a Vertex Class that can order a Product, another Vertex Class, by creating an Ordered Edge from User to Product.

### OrientServer & OrientClient

To use all the functionality an `OrientClient` instance needs to be implictily in scope, providing information on how to connect to OrientDB etc. To create `OrientClient`  an `OrientServer` needs to be implicitly in scope as well, defining whether the server is located `InMemory`, `Embedded` or `Remote`.  An OrientClient can be created in two different ways, first by calling the init function and providing literal arguments and secondly, using an `Configuration` object from the Typesafe configuration library.

```scala
val configuration = TypesafeConfig // TODO

// TODO
implicit val orientServerMemory: OrientServer = OrientServer.InMemory

// TODO
implicit val orientServerEmbedded: OrientServer = OrientServer.Embedded

// TODO
implicit val orientServerRemote: OrientServer = OrientServer.Remote

// TODO
implicit val orientClient: OrientClient = OrientClient.init(
  uri      = "remote:local",
  database = "root",
  user     = "root",
  password = "root",
  home     = "root")  

//  
implicit val orientClientConfig: OrientClient = OrientClient.fromConfig(configuration)
```

The `application.conf` for the `Configuration` object needs to have the following fields.

```yaml
oriented.uri      = "remote:localhost"
oriented.database = "root"
oriented.user     = "root"
oriented.password = "root"
orientdb.home     = "orientdb" # The directory where OrientDB persists during development
```

### OrientFormat Typeclass

Explain… (See feature list for support of creating instances of OrientFormat with Shapeless TODO)

```scala
trait OrientFormat[A] {

}

object User {
  implicit val userFormat: OrientFormat[User] = new OrientFormat[User] {

  }
}
```

### OrientResult

Explain OrientResult etc...

### Creating a Vertex

To create a Vertex Class (not an insert) call the vertex function on the client instance , it takes one type parameter specifying the type of the Vertex (OrientFormat[User] in this example needs to be implicitly in scope).

```scala
// Creates a Vertex Class of type User
// OrientFormat[User] needs to be implicitly in scope
val userVertexClass: OrientResult[VertexClass[User]] = client.vertex[User]

// Visit result options with a function specifying the action based on the result
// Results in Future[Unit] due to the print statement
userVertexClass.fold(
  error => println(s"An error occured $error"),
  vc    => println(s"Vertex Class created: $vc"))
```

### Inserting a Vertex

To insert a new vertex in the defined class, the sql interpolation function can be used. Inside the string the extend SQL query for orientdb is defined. Then the vertex function is called specifying the type parameter of the `Vertex` class. The insert function defines, specifies it being an orientdb insert statement.

```scala
def createUser(name: String, age: Int, address: String): OrientResult[Vertex[User]] =
  sql"INSERT INTO User values($name, $age, $address)".vertex[User].insert.run

def createProduct(name: String, price: Double): OrientResult[Vertex[Product]] =
  sql"INSERT INTO Product values($name, $price)".vertex[User].insert.run

// Sequencing multiple OrientResults  
val userProductVertices: OrientResult[(Vertex[User], Vertex[Product])] =
  for {
     userVertex    <- createUser("Test user", 30, "Test Street")
     productVertex <- createProduct("Test Product", 100.25)  
  } yield (userVertex, productVertex)

// To extract the data from the case class definitions
val userProduct: OrientResult[(User, Product)] = userProductVertices.map {
  case (userVertex, productVertex) => (userVertex.value, productVertex.value)
}
```

### Creating an Edge

Edge Classes get created the same way as vertices, using the edge function on the client instance.

```scala
// Creates a Edge Class definition of type Ordered
val orderedEdgeClass: OrientResult[EdgeClass[Ordered]] = client.edge[Ordered]

orderedEdgeClass.fold(
  error => println(s"An error occured $error"),
  ec    => println(s"Edge Class created: $ec"))
```

### Inserting an Edge

Inserting an Edge is almost the same inserting an Vertex, however the `edge` function is used and the `Edge` type is returned.

```scala
val today: String = DateTime.now().toString() // or however you create your date(time)s

def createOrdered(orderId: String, amount: Int): OrientResult[Edge[Ordered]] =
  sql"INSERT INTO Ordered values ($orderId, $amount, $today".edge[Ordered].insert.run
```

### Add Edge to Vertices

Once two vertices are created they can be connected to each other by an Edge. This can be done in one for comprohension, sequencing the result of each OrientResult.

```scala
def addEdgeToVertices: OrientResult[(Vertex[User], Vertex[Product], Edge[Ordered])] = 
  for {
    userVertex    <- createUser("Test user", 30, "Test Street")
    productVertex <- createProduct("Test Product", 100.25)
    orderedEdge   <- userVertex.addEdge(productVertex, Ordered("test-id", 5))
  } yield (userVertex, productVertex, orderedEdge)
```

### Querying Simple Types

If the result type of an OrientResult is a simple type such as `String` or `Int`, then there is no need to write a special `OrientFormat` instance for these types. Below is an example shown on how to query simple types.

```scala
def countProducts: OrientResult[Long] =
  sql"SELECT COUNT(*) as count FROM Facility".as[Long]("count").run.map(_.value)
```

### Querying Vertices

Vertices can be queried using the extended SQL notation from OrientDB. By using the sql interpolation function, like inserting, the query can be created. The vertex function specifies the type parameter of the Vertex class. After this the result context is specified with another function, such as list, single, opt. Once the context is specified the query can be composed or executed by calling the run function. 

```scala
// List of results
def findAllUsers: OrientResult[List[Vertex[User]]] =
  sql"SELECT * FROM User".vertex[User].list.run

// Extract the result  
findAllUsers.fold(
  error => println(s"An error occured $error"),
  users => println(s"Found users: ${users.map(_.value).mkString(",")}."))

// Optional result  
def findUser(name: String): OptionalOrientResult[Vertex[User]] =
  sql"SELECT * FROM User where name == '$name'".vertex[User].opt.run

// Extract the result with three functions: onError, onNone and onSome  
findUser("Test User").fold(
  error => println(s"An error occured $error"),
  ()    => println(s"User could not be found"),
  user  => println(s"Found user $user"))  
```

### Querying Edges

Querying edges is the same as vertices, only the edge function is called after the sql interpolation specifying the type parameter of the resulting Edge class. 

```scala
def findByOrderId(orderId: String): OrientResult[List[Edge[Ordered]]] = 
  sql"SELECT * FROM Ordered WHERE orderId = $orderId".edge[Ordered].list.run
  
fed findById(id: String): OptionalOrientResult[Edge[Ordered]] =
  sql"SELECT * FROM Ordered WHERE id = $id".edge[Ordered].opt.run
```

### Updating Vertices & Edges

```scala

```

### Actions on Vertex & Edge

The result type of querying or inserting is an OrientResult or alike, inside this OrientResult however is another typeclass the Vertex or Edge. This typeclass makes the original OrientVertex and OrientEdge more typeful and allows calling methods to sequence multiple actions together. See the ScalaDocs for a full overview of all the actions available on the Vertex and Edge classes.

```scala

```

## Documentation and Support

* ScalaDocs (TODO)
* Extended Guide (TODO)
* [OrientDB Documentation](http://orientdb.com/docs/last/)
* [OrientDB.com](http://www.orientdb.com)
* [OrientDB Github Repo](https://github.com/orientechnologies/orientdb)

## Design Choices

__TODO:__ This section will explain the design choices made in developing this wrapper, which will be open for discussion and improvement!

## Feature List

Below a list of wanted features is shown, this list is however not yet complete, if you have anyhting you want to add to the list please submit a PR. Features that are checked are implemented, others still have to be done (your help is welcome see Contributing below).

- [ ] a task list item
- [x] list syntax required

## Contributing

- Guidelines
- Running Test
