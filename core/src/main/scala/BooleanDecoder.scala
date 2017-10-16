package wiro

import io.circe.Decoder
import scala.util.Try

trait CustomBooleanDecoder {
  private[this] def asBoolean(s: String) = Try(s.toBoolean)
  implicit final val decodeBoolean: Decoder[Boolean] =
    Decoder.decodeBoolean or Decoder.decodeString.emapTry(asBoolean).withErrorMessage("Boolean")
}
