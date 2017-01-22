package wiro.server.akkaHttp

import io.circe._
import io.circe.syntax._

object FailSupport {
  case class FailException[T: ToHttpResponse](hasResponse: T) extends Throwable {
    def response = implicitly[ToHttpResponse[T]].response
  }

  implicit def wiroCanFailEncoder[T: ToHttpResponse, A: Encoder] = new WiroEncoder[Either[T, A]] {
    def encode(d: Either[T, A]): Json = d match {
      case Right(result) => result.asJson
      case Left(error) => throw FailException(error)
    }
  }
}
