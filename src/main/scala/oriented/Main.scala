package oriented

import java.util.Date
import com.tinkerpop.blueprints.impls.orient.OrientElement
import oriented.syntax._

object Main extends App {

  implicit val client: OrientClient = InMemoryClient("MyDB", pool = Some(1 -> 10))

  case class User(name: String, occupation: String)

  implicit val userFormat = new OrientFormat[User] {

    def format(element: OrientElement): User = User(
      element.getProperty[String]("name"),
      element.getProperty[String]("occupation"))

    def name: String = "User"

    def properties(user: User): Map[String, Any] =
      Map("name" -> user.name, "occupation" -> user.occupation)

  }

  case class Likes(date: Date)

  implicit val likesFormat = new OrientFormat[Likes] {
    override def format(element: OrientElement): Likes = Likes(
      element.getProperty[Date]("date")
    )

    override def name: String = "Likes"

    override def properties(model: Likes): Map[String, Any] = Map("date" -> model.date)
  }

  case class Article(title: String, content: String)

  implicit val articleFormat = new OrientFormat[Article] {
    override def format(element: OrientElement): Article = Article(
      element.getProperty[String]("title"),
      element.getProperty[String]("content")
    )

    override def name: String = "Article"

    override def properties(model: Article): Map[String, Any] =
      Map("title" -> model.title, "content" -> model.content)
  }

//  val orientAction: OrientAction[(Vertex[User], Vertex[Article], Edge[Likes])] = for {
//    userVertexType    <- client.createVertexType[User]
//    articleVertexType <- client.createVertexType[Article]
//    likesEdgeType     <- client.createEdgeType[Likes]
//    userVertex        <- client.addVertex(User("Thomas", "Developer!"))
//    articleVertex     <- client.addVertex(Article("Monads are awesome!", ">>="))
//    likesEdge         <- client.addEdge(Likes("today"), articleVertex, userVertex)
//  } yield (userVertex, articleVertex, likesEdge)

//  val orientAction = for {
//    articleVertex <- client.addVertex(Article("Test article", "some content"))
//    baseClassName <- articleVertex.getBaseClassName
//    elementType   <- articleVertex.getElementType
//    identity      <- articleVertex.getIdentity
//    label         <- articleVertex.getLabel
//    _ <- articleVertex.remove
//    articles <- sql"SELECT * FROM Article".vertex[Article].list
//  } yield articles

//  val orientAction = for {
//      articleVertex <- client.addVertex(Article("Test article", "some content"))
//      userVertex    <- client.addVertex(User("Thomas", "DEVELOPER!!!"))
//      edge          <- client.addEdge(Likes("today"), articleVertex, userVertex)
//      vertices      <- edge.getVertices[Article, User]
//    } yield vertices



  val userLikesArticle =
    User("Thomas", "Developer") -- Likes(new Date()) --> Article("Monads are awesome", "Some content")

//  val userLikesArticleVertices: OrientAction[(Vertex[User], Edge[Likes], Vertex[Article])] = for {
//    userVertex    <- client.addVertex(User("Thomas", "Developer"))
//    articleVertex <- client.addVertex(Article("Monads", "Cool"))
//    likes         <- userVertex -- Likes("today") --> articleVertex
//  } yield (userVertex, likes, articleVertex)

//  val orientAction = for {
//    userType      <- client.createVertexType[User]
//    articleType   <- client.createVertexType[Article]
//    likesType     <- client.createEdgeType[Likes]
//    userVertex    <- client.addVertex(User("Thomas", "Developer"))
//    articleVertex <- client.addVertex(Article("OrientDB with Free", "Content"))
//    edge          <- userVertex.addEdge(Likes("Today"), articleVertex)
//    edgeTwo       <- articleVertex.addEdge(Likes("Today"), userVertex)
//    t             <- articleVertex.countEdges[Likes](Out)
//    t2            <- articleVertex.getType
//    t3            <- userVertex.getVertices[Likes, Article](In)
//    edges         <- userVertex.getEdges[Article, Likes](articleVertex, Both)
//  } yield edges

  val unsafeResult: (Vertex[User], Edge[Likes], Vertex[Article]) =
    runUnsafe(userLikesArticle, enableTransactions = false)
//
//  val safeResult = toOrientActionInterpreter(orientAction).runSafe
//
//  val futureResult = toOrientActionInterpreter(orientAction).runAsyncUnsafe

  //val futureSafeResult: EitherT[Future, Throwable, List[Edge[Likes]]] = toOrientActionInterpreter(orientAction).runAsyncSafe

  //val safeResult: Either[Throwable, (Vertex[User], Vertex[Article], Edge[Likes])] = orientAction.runSafe

  //val tryResult: Try[(Vertex[User], Vertex[Article], Edge[Likes])] = orientAction.tryRun

//  val unsafeAsyncResult: Future[(Vertex[User], Vertex[Article], Edge[Likes])] = orientAction.runAsyncUnsafe
//
//  val safeAsyncResult: EitherT[Future, Throwable, (Vertex[User], Vertex[Article], Edge[Likes])] = orientAction.runAsyncSafe

  println(unsafeResult) // success


  val unsafeResultTwo = runUnsafe(sql"SELECT * FROM User".vertex[User].nel)

  println(unsafeResultTwo)
//
//  println(safeResult)
//
//  println(futureResult)


  //print(safeResult.toString) // error
  //println(tryResult.toString) // error


//  case class Account(userName: String, firstName: String, lastName: String, picture: String, description: String)
//  case class Tweet(content: String)
//
//  case class Posted(date: Date)
//  case class Likes(date: Date)
//  case object Follows

}
