package wiro.server

import akka.http.scaladsl.model.HttpResponse
import io.circe.Json

package object akkaHttp {
  final case class ReferenceConfig(routesPrefix: Option[String], ciao: String)

  trait ToHttpResponse[A] {
    def response(a: A): HttpResponse
  }

  trait WiroEncoder[A] {
    def encode(a: A): Json
  }
}
