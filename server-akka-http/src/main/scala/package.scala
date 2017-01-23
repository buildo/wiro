package wiro.server

import akka.http.scaladsl.model.HttpResponse
import io.circe.Json

package object akkaHttp {
  trait ToHttpResponse[A] {
    def response(a: A): HttpResponse
  }

  trait WiroEncoder[A] {
    def encode(a: A): Json
  }
}
