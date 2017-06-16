package wiro

import scala.annotation.StaticAnnotation
import wiro.server.akkaHttp.AuthenticationType

package object annotation {
  class command(name: Option[String] = None) extends StaticAnnotation
  class query(name: Option[String] = None) extends StaticAnnotation
  class auth(authenticationType: AuthenticationType) extends StaticAnnotation
  class path(name: String) extends StaticAnnotation
}
