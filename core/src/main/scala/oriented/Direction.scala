package oriented

/**
  * Provide direction for Edges and Vertices actions
  */
sealed trait Direction
case object In extends Direction
case object Out extends Direction
case object Both extends Direction
