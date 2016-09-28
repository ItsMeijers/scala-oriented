package oriented

import com.tinkerpop.blueprints.impls.orient.OrientElement

/**
  * OrientFormat typeclass makes it able to transform from and to OrientElements from a specific model A.
  */
trait OrientFormat[A] {

  /**
    * Formats an OrientElement to the model of type A
    */
  def format(element: OrientElement): A

  /**
    * The name of the Model (class name)
    */
  def name: String

  /**
    * A Map of properties where each name of the property of the class is the String and Any is the value.
    * TODO: Change to Shapeless implementation
    */
  def properties(model: A): Map[String, Any]

  // case class User(id: Int, name: String, birthday: Date)
  // name: "User"
  // nameTypes: "id" -> Int, "name" -> String, "birthday" -> Date
  // values= User(1, "Thomas", Date("...")) -> 1, "Thomas", Date(...)
  // properties = nameTypes.map(_._1) zip values
  // format = nameTypes.map(case (n, t) -> element.getProperty[t](n)) ==> User

}
