# scala-oriented [![Build Status](https://travis-ci.org/ThomasMeijers/scala-oriented.svg?branch=master)](https://travis-ci.org/ThomasMeijers/scala-oriented)

**_scala-oriented_** is a Scala wrapper for the OrientDB Java API, _oriented_ on functional and typeful programming. The library uses the extended SQL queries and commands from the Java API,  encapsulating side-effect and queries resulting in meaningful types.  

## Quickstart

Releases and dependencies are shown below.

| scala-oriented |  status  | JDK  |    scala    | Graph-DB | Server |
| :------------: | :------: | :--: | :---------: | :------: | :----: |
|      0.1       | unstable | 1.8  | 2.10 & 2.11 |  2.2.7   | 2.2.7  |

_Note that scala-orient is still in very early development and will undergo many changes._

To use scala-oriented add the following library dependency to your `build.sbt`.

```scala
libraryDependencies += "com.itsmeijers" %% "scala-oriented" % "0.1"
```

Two imports are advised to use scala-orient  (the rest of this readme assumes that these imports are in scope).

```scala
import oriented._        // Imports all required types
import oriented.syntax._ // Imports syntax to work with scala-oriented
```

The API Docs can be found at [itsmeijers.com/docs/scala-oriented/](http://itsmeijers.com/docs/scala-oriented/).

## Common Use Cases

The next sections will show code examples of common use cases, using scala-oriented. This is intended to show how the library should be used. In the examples a _"Twitter like"_ graph structure will be shown, the following case classes need to be defined to follow along with the examples.

```scala
import java.util.Date

// Data definitions of the Vertices
case class User(name: String, description: String)
case class Tweet(content: String, postDate: Date)
  
// Date definitions of the Edges  
case class Follows(from: Date)
case object Tweets
```

### OrientClient

For connections with OrientDB an `OrientClient` needs to be implicitly in scope when an action is ran. There are three types of OrientClients; `InMemoryClient`, `PLocalClient` and `RemoteClient`. For demonstration purposes the local client is used, however inialization of PLocal and Remote are almost identical.

```scala
implicit val client: OrientClient = InMemoryClient(db = "TestDB") 
```

### OrientFormat Typeclass

For now, each vertex and edge needs to have its own `OrientFormat` instance (I hope to provide automatic derivation in the very near future), that needs to be implicitly in scope. The typeclass instances provide the needed information to read/write case classes to/from OrientDB.

```scala
implicit val userFormat: OrientFormat[User] = new OrientFormat[User] {
  def name: String = "User"

  def properties(user: User): Map[String, Any] = 
    Map("name" -> user.name, "description" -> user.description)

  def read: OrientRead[User] =
    for {
      name        <- readString("name")
      description <- readString("description")
    } yield User(name, description)
}

implicit val tweetFormat: OrientFormat[Tweet] = new OrientFormat[Tweet] {
  def name: String = "Tweet"

  def properties(tweet: Tweet): Map[String, Any] = 
    Map("content" -> tweet.content, "postDate" -> tweet.postDate)

  def read: OrientRead[Tweet] =
    for {
      content  <- readString("content")
      postDate <- readDatetime("postDate")
    } yield Tweet(content, postDate)
}

implicit val followsFormat: OrientFormat[Follows] = new OrientFormat[Follows] {
  def name: String = "Follows"

  def properties(follows: Follows): Map[String, Any] = Map("from" -> follows.from)

  def read: OrientRead[Follows] = readDatetime("from").map(Follows)
}

implicit val tweetsFormat: OrientFormat[Tweets.type] = new OrientFormat[Tweets.type] {
  def name: String = "Tweets"

  def properties(tweets: Tweets.type): Map[String, Any] = Map()

  def read: OrientRead[Tweets.type] = read(Tweets)
}
```

As can be seen in the examples, the name represents the name of the Vertex or Edge. Properties is a function from the model to a Map where the keys are the fieldnames of the class and the values are the corresponding value of the model. Read needs an `OrientRead` instance of the model. An `OrientRead` can be constructed using the read functions, where each supported type in OrientDB has a read function. The `read` function can be used for objects or fieldless case classes, to lift the value into `OrientRead`. Each read function returns an `OrientRead`, that can be sequenced together to the model (using map, flatMap or for-comprehension). These format instances are much boilerplate, and will be changed soon into automatic derivation.

### Running OrientIO

Each action that is build to be sent to OrientDB is of type `OrientIO[A]`, where `A` is the result type of the graph action(s). To run these actions (and actually execute them) different functions can be used, encapsulating side effect in different ways. Each function also handles the opening/closing of the graph instances and commits plus rollbacks (on errors) when transactions are enabled.

The `runGraph` function runs the `OrientIO[A]` in a safe manner, resulting in an `Either[Throwable, A]`.

```scala
// transaction is enabled by default
val e: Either[Throwable, User] = orientIO.runGraph 
// without transactions  
val eNoTx: Either[Throwable, User] = orientIO.runGraph(enableTransaction = false)
```

Below an overview of each function available on `OrientIO[A]` for running the actions. Transactions are enabled by default, for running the graph actions without transactions set the `enableTransactions` parameter to `false`. 

| Function             | Return type                   | Description                              |
| -------------------- | ----------------------------- | ---------------------------------------- |
| runGraph             | Either[Throwable, A]          | Runs the graph action(s) safely          |
| runGraphUnsafe       | A                             | Runs the graph actions(s) unsafely, this can throw errors (adviced to only use in tests) |
| tryGraphRun          | Try[A]                        | Runs the graph action(s) safely, encapsulating side effect in a Try. |
| runGraphAsync*       | EitherT[Future, Throwable, A] | Runs the graph action(s) asynchronously and safe |
| runGraphAsyncUnsafe* | Future[A]                     | Runs the graph action(s) asynchronously but unsafe |

__* Experimental feature: OrientDB elements are not thread-safe.__

### Creating a VertexType

Vertex schemas _can_ be created before inserting any vertices, resulting in an `VertexType[A]`, where `A` is the vertex model. The `createVertexType[A]` function is available on the client.

```scala
// Create a VertexType of type User (OrientFormat[User] needs to be implicitly in scope)
val uType: OrientIO[VertexType[User]] = client.createVertexType[User]

// OrientIO Actions can be sequenced
val vTypes: OrientIO[(VertexType[User], VertexType[Tweet])] =
  for {
    userType  <- client.createVertexType[User]
    tweetType <- client.createVertexType[Tweet]
  } yield (userType, tweetType)

// Running the action
val vTypesResult: (VertexType[User], VertexType[Tweet]) = 
    vertexTypes.runGraphUnsafe(enableTransactions = false)
  
println(vTypesResult)
// [info] (VertexType(User),VertexType(Tweet))
```

### Adding a Vertex

Adding a Vertex is done by calling the `addVertex[A](a: A)` function from the client, where `A` is the vertex you want to save to OrientDB. It results in a `Vertex[A]`, when running the graph action. Vertex is a typeclass, that provides typed functions to the OrientVertex element. You can access the model in the `element` property. For the the functions on Vertex, see the API Docs or your favourite auto-completion.

```scala
// Adding a single Vertex
val user: OrientIO[Vertex[User]] = client.addVertex(User("Thom", "Noobgrammer"))
val userResult: Vertex[User] = user.runGraphUnsafe

println(userResult)
// [info] Vertex(User(Thomas,Noobgrammer),v(User)[#25:0])
  
// Sequence off adding vertices
val users: OrientIO[List[Vertex[User]]] =
  for {
    joe    <- client.addVertex(User("Joe", "Manager"))
    bob    <- client.addVertex(User("Bob", "Programmer"))
    elodie <- client.addVertex(User("Elodie", "Programmer"))
  } yield List(joe, bob, elodie)

val usersResult: List[Vertex[User]] = users.runGraphUnsafe

usersResult.foreach(println)
// [info] Vertex(User(Joe,Manager),v(User)[#26:0])
// [info] Vertex(User(Bob,Programmer),v(User)[#27:0])
// [info] Vertex(User(Elodie,Programmer),v(User)[#28:0])
```

### Creating an EdgeType

Same as `VertexType`, only for edges. Returns an `EdgeType[A]`, where `A` is the edge model.

```scala
val eTypes: OrientIO[(EdgeType[Tweets], EdgeType[Follows])] = 
  for {
    tweetsType  <- client.createEdgeType[Tweets.type]
    followsType <- client.createEdgeType[Follows]  
  } yield (tweetsType, followsType)
    
val eTypesResult: (EdgeType[Tweets.type], EdgeType[Follows]) = 
  eTypes.runGraphUnsafe(enableTransactions = false)
  
println(eTypesResult)
// [info] (EdgeType(Tweets),EdgeType(Follows))
```

### Adding an Edge

An edge gets created between to vertices, by calling the `addEdge(edgeModel: A, inVertex[Vertex[B]], outVertex: Vertex[C])` function on client. The Edge typeclass is similair to the Vertex typeclass, but represents a typed Edge. The Edge model can be retrieved from the `element` property. The `Edge` typeclass currently has four functions: `getInVertex`, `getOutVertex`, `getVertices` (which is a combination of in and out) and `update(newModel: A)` (See updating Updating Vertices & Edges).

```scala
// User vertices taken from previous example
val thomasFollowsJoe: OrientIO[Edge[Follows]] =
    client.addEdge(Follows(new Date()), userResult, usersResult.head)

val tFollowsJResult: Edge[Follows] = thomasFollowsJoe.runGraphUnsafe

println(tFollowsJResult)
// [info] Edge(Follows(Mon Oct 03 15:00:42 CEST 2016),e[#49:0][#26:0-Follows->#25:0])
```

### Adding an Edge from the Vertex Typeclass

An edge can also be added from a Vertex instance, by calling the`addVertex(edgeModel: A, outVertex: Vertex[B]): Edge[C]` function. The function will return the created edge between the in and out vertices. Below are a couple of examples shown of creating vertices and edges.

```scala
// Vertex[User] -- Edge[Follows] --> Vertex[User]
val userFollowsUser: OrientIO[(Vertex[User], Edge[Follows], Vertex[User])] = 
  for {
    bert    <- client.addVertex(User("Bert", "DevOps"))
    ernie   <- client.addVertex(User("Ernie", "Manager"))
    follows <- bert.addEdge(Follows(new Date()), ernie)
  } yield (bert, follows, ernie)

val ufuResult: (Vertex[User], Edge[Follows], Vertex[User]) = 
  userFollowsUser.runGraphUnsafe

println(ufuResult)
// [info] (Vertex(User(Bert,DevOps),v(User)[#25:1]),Edge(Follows(Mon Oct 03 19:31:03 CEST 2016),e[#53:0][#25:1-Follows->#26:1]),Vertex(User(Ernie,Manager),v(User)[#26:1]))

// Vertex[User] -- Edge[Tweets] --> Vertex[Tweet]  
val userTweetsTweet: OrientIO[(Vertex[User], Edge[Tweets.type], Vertex[Tweet])] =
  for {
    user   <- client.addVertex(User("Erik", "FP"))
    tweet  <- client.addVertex(Tweet("Developers talk semicolons", new Date()))
    tweets <- user.addEdge(Tweets, tweet)
  } yield (user, tweets, tweet)

val uttResult: (Vertex[User], Edge[Tweets.type], Vertex[Tweet]) =
  userTweetsTweet.runGraphUnsafe
  
println(uttResult)    
// [info] (Vertex(User(Erik,FP),v(User)[#27:1]),Edge(Tweets,e[#41:0][#27:1-Tweets->#33:0]),Vertex(Tweet(Developers talk semicolons,Mon Oct 03 19:39:19 CEST 2016),v(Tweet)[#33:0]))
  
// Vertex[User] <-- Edge[Follows] --> Vertex[User]
val bidirectionalFollow: OrientIO[(Edge[Follows], Edge[Follows])] = 
  for {
    hank      <- client.addVertex(User("Hank", "Developer"))
    suzan     <- client.addVertex(User("Suzan", "Developer"))
    hFollowsS <- hank.addEdge(Follows(new Date()), suzan)
    sFollowsH <- suzan.addEdge(Follows(new Date()), hank)
  } yield (hFollowsS, sFollowsH)

val bidirectionalResult: (Edge[Follows], Edge[Follows]) =
  bidirectionalFollow.runGraphUnsafe

println(bidirectionalResult)
// [info] (Edge(Follows(Mon Oct 03 15:16:48 CEST 2016),e[#50:0][#29:0-Follows->#30:0]),Edge(Follows(Mon Oct 03 15:16:48 CEST 2016),e[#51:0][#30:0-Follows->#29:0]))
```

### Querying Simple Types

If the result type of an OrientResult is a simple type such as `String` or `Int`, then there is no need to write a special `OrientFormat` instance for these types. Instead of using the vertex or edge function on the sql query, use the `as[A](field)` function where `A` is the simple type and field is the  `String` representing the field that you want to select as the simple type.

```scala
val numberOfUsers: Long = sql"SELECT COUNT(*) as count FROM User"
  .as[Long]("count")
  .runGraphUnsafe

println(numberOfUsers)
// [info] 11 
```

### Querying

SQL queries can be contstructed using the `sql` interpolation modifier.

```scala
val sqlStatement: SQLStatement = sql"SELECT * FROM User"
```

This creates a SQLStatement object which has the following function showed in the table below.

| Function               | Return Type      | Description                              |
| ---------------------- | ---------------- | ---------------------------------------- |
| `vertex[A]`            | `VertexQuery[A]` | Creates a query that will result in a `Vertex[A]`. |
| `edge[A]`              | `EdgeQuery[A]`   | Creates a query that will result in an `Edge[A]` |
| `update`               | `Unit`           | Creates a query that will result in Unit. |
| `as[A](field: String)` | `OrientIO[A]`    | See Querying Simple Types.               |

The `vertex[A]` and `edge[A]` function return a `VertexQuery[A]` or `EdgeQuery[A]` that needs one more function call to specify its context, resulting in an `OrientIO[R]`, where `R` is for instance `Option[Vertex[A]]`.

| Function | Vertex Return Type        | Edge Return Type        | Description                              |
| :------- | ------------------------- | ----------------------- | ---------------------------------------- |
| `unique` | `Vertex[A]`               | `Edge[A]`               | Returns a unique element.                |
| `option` | `Option[Vertex[A]]`       | `Option[Edge[A]]`       | Returns a optional element. If there are more than one elements only the first gets returned as a Some. |
| `nel`    | `NonEmptyList[Vertex[A]]` | `NonEmptyList[Edge[A]]` | Returns a non empty list.                |
| `list`   | `List[Vertex[A]]`         | `List[Edge[A]]`         | Returns a list.                          |

### Querying Vertices Examples

Below are some examples shown of querying vertices.

```scala
TODO
```

### Querying Edges Examples

Below are some examples shown of querying edges.

```scala
TODO
```

### Updating Vertices & Edges

Below are some examples shown of updating edges and vertices with both queries and functions on the `Vertex` and `Edge` typeclasses. Note that the functions return the new vertices and models, where updates on queries do not.

```scala
TODO
```

## Documentation and Support

- [API Docs](http://itsmeijers.com/docs/scala-oriented/)
- [OrientDB Documentation](http://orientdb.com/docs/last/)
- [OrientDB.com](http://www.orientdb.com)
- Extended Guide (TODO)

## Feature List

_TODO_

## Contributing

_TODO_