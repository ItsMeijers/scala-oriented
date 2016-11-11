package oriented

import com.orientechnologies.orient.client.remote.OServerAdmin
import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphFactory, OrientGraphNoTx}
import oriented.free.dsl._
import oriented.syntax.{OrientIO, OrientProgram}

/**
  * OrientClient provides communication to OrientDB, with either a InMemory, (P)Local or Remote database.
  */
sealed trait OrientClient {

  /**
    * Connection uri to OrientDB.
    */
  def uri: String

  /**
    * Database name for connecting to the database.
    */
  def db: String

  /**
    * User string for authentication.
    */
  def user: String

  /**
    * User password for authentication.
    */
  def password: String

  /**
    * Returns a graph instance that has transactions disabled.
    */
  def graphNoTransaction: OrientGraphNoTx = factory.getNoTx

  /**
    * Returns a graph instances that has transactions enabled.
    */
  def graph: OrientGraph = factory.getTx

  /**
    * Creates the server admin for type of connection to OrientDB.
    */
  def serverAdmin: OServerAdmin

  /**
    * Factory for creation of the Graphs.
    * Needs to be lazy for initializing in trait.
    */
  lazy val factory: OrientGraphFactory = pool.map { case (min, max) =>
      new OrientGraphFactory(uri).setupPool(min, max)
    }.getOrElse(new OrientGraphFactory(uri))

  /**
    * Optional connection pool parameters, tuple represents minimal to maximal connections.
    */
  def pool: Option[(Int, Int)]

  val C: Clients[OrientProgram] = Clients.clients[OrientProgram]

  /**
    * Action on the client that creates a OrientIO for creating a VertexType of A.
    */
  def createVertexType[A](implicit orientFormat: OrientFormat[A]): OrientIO[VertexType[A]] =
    C.createVertexType[A](orientFormat)

  /**
    * Action on the client that creates a OrientIO for creating an EdgeType of A.
    */
  def createEdgeType[A](implicit orientFormat: OrientFormat[A]): OrientIO[EdgeType[A]] =
    C.createEdgeType[A](orientFormat)

  /**
    * Action on the client that creates an OrientIO which creates a Vertex in the database.
    */
  def addVertex[A](vertexModel: A)(implicit orientFormat: OrientFormat[A]): OrientIO[Vertex[A]] =
    C.addVertex[A](vertexModel, orientFormat)

  /**
    * Action on the client that creates an OrientIO which creates an Edge link between to vertices in the database.
    */
  def addEdge[A, B, C](edgeModel: A,
                       inVertex: Vertex[B],
                       outVertex: Vertex[C])
                      (implicit orientFormat: OrientFormat[A]): OrientIO[Edge[A]] =
    C.addEdge[A, B, C](edgeModel, inVertex, outVertex, orientFormat)

  def shutdown(close: Boolean = false): OrientIO[Unit] =
    C.shutdown(close)
}

/**
  * Creates an InMemory Client for OrientDB.
  */
case class InMemoryClient(db: String, user: String = "root", password: String = "root", pool: Option[(Int, Int)] = None) extends OrientClient {

  val uri: String = s"memory:$db"

  val serverAdmin: OServerAdmin = new OServerAdmin(uri)

}

/**
  * Creates an PLocal Client for OrientDB.
  */
case class PLocalClient(uri: String, db: String, user: String, password: String, pool: Option[(Int, Int)] = None) extends OrientClient {

  val serverAdmin: OServerAdmin = new OServerAdmin(s"$uri/$db").connect(user, password)

}

/**
  * Creates an Remote Client for OrientDB.
  */
case class RemoteClient(uri: String, db: String, user: String, password: String, pool: Option[(Int, Int)] = None) extends OrientClient {

  val serverAdmin: OServerAdmin = new OServerAdmin(uri).connect(user, password)

}
