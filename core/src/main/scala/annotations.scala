package wiro

import wiro.server.akkaHttp.AuthenticationType

package object annotation {
  case class command(name: Option[String] = None) extends scala.annotation.StaticAnnotation
  case class query(name: Option[String] = None) extends scala.annotation.StaticAnnotation
  case class auth(authenticationType: AuthenticationType) extends scala.annotation.StaticAnnotation
}
