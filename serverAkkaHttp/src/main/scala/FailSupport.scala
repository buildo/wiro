package wiro.server.akkaHttp

import io.circe._
import io.circe.syntax._

object FailSupport {
  case class FailException[T: ToHttpResponse](hasResponse: T) extends Throwable {
    def response = implicitly[ToHttpResponse[T]].response(hasResponse)
  }

  implicit def wiroCanFailEncoder[T: ToHttpResponse, A: Encoder] = new WiroEncoder[Either[T, A]] {
    def encode(d: Either[T, A]): Json = d.fold(error => throw FailException(error), _.asJson)
  }
}
