package wiro

import io.circe._
import scala.util.{ Try, Success, Failure }

//Shamelessy copied by circe, author is @travisbrown
trait CustomBooleanDecoder {
  implicit final val decodeBoolean: Decoder[Boolean] = new Decoder[Boolean] {
    private[this] def fail(c: HCursor) = Left(DecodingFailure("Boolean", c.history))

    final def apply(c: HCursor): Decoder.Result[Boolean] = c.value.asBoolean match {
      case Some(b) => Right(b)
      case None => c.value.asString match {
        case Some(s) => Try(s.toBoolean) match {
          case Success(v) => Right(v)
          case Failure(_) => fail(c)
        }
        case None => fail(c)
      }
    }
  }
}
