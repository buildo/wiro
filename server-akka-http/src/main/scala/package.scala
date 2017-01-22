package wiro.server

import akka.http.scaladsl.model.HttpResponse
import io.circe.Json

package object akkaHttp {
  trait ToHttpResponse[T] {
    def response: HttpResponse
  }

  trait WiroEncoder[A] {
    def encode(a: A): Json
  }
}
