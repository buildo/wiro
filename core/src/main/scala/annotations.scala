package wiro

import scala.annotation.StaticAnnotation

package object annotation {
  class command(name: Option[String] = None) extends StaticAnnotation
  class query(name: Option[String] = None) extends StaticAnnotation
  class path(name: String) extends StaticAnnotation
}
