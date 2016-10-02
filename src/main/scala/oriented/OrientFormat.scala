package oriented

import java.util.Date

import cats.Id
import cats.data.Reader
import cats.free.Free
import com.tinkerpop.blueprints.impls.orient.OrientElement
import oriented.free.dsl._
import oriented.free.interpreters.ReadInterpreter
import oriented.syntax.OrientRead

/**
  * OrientFormat typeclass makes it able to transform from and to OrientElements from a specific model A.
  */
trait OrientFormat[A] {

  lazy val reader: Reader[OrientElement, A] = read.foldMapUnsafe(ReadInterpreter)

  /**
    * Formats an OrientElement to the model of type A
    */
  def read: OrientRead[A]

  /**
    * The name of the Model (class name)
    */
  def name: String

  /**
    * A Map of properties where each name of the property of the class is the String and Any is the value.
    * TODO: Change to Shapeless implementation
    */
  def properties(model: A): Map[String, Any]

  private val element: Reads[ReadDSL] = Reads.reads[ReadDSL]

  def read[R](r: R): OrientRead[R] = element.read(r)

  def readBoolean(fieldName: String): OrientRead[Boolean] = element.readBoolean(fieldName)

  def readInt(fieldName: String): OrientRead[Int] = element.readInt(fieldName)

  def readShort(fieldName: String): OrientRead[Short] = element.readShort(fieldName)

  def readLong(fieldName: String): OrientRead[Long] = element.readLong(fieldName)

  def readFloat(fieldName: String): OrientRead[Float] = element.readFloat(fieldName)

  def readDouble(fieldName: String): OrientRead[Double] = element.readDouble(fieldName)

  def readDatetime(fieldName: String): OrientRead[Date] = element.readDatetime(fieldName)

  def readString(fieldName: String): OrientRead[String] = element.readString(fieldName)

  def readBinary(fieldName: String): OrientRead[List[Byte]] = element.readBinary(fieldName)

  // for {
  // id <- read[Int]
  // name <- read[String]
  // birthday <- read[Date]
  // yield User(id, name, birthday)

  // case class User(id: Int, name: String, birthday: Date)
  // name: "User"
  // nameTypes: "id" -> Int, "name" -> String, "birthday" -> Date
  // values= User(1, "Thomas", Date("...")) -> 1, "Thomas", Date(...)
  // properties = nameTypes.map(_._1) zip values
  // format = nameTypes.map(case (n, t) -> element.getProperty[t](n)) ==> User

}
