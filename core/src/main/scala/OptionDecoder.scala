package wiro

import io.circe._
import cats.data.Validated

//Shamelessy copied by circe, author is @travisbrown
trait CustomOptionDecoder {
  final def withReattempt[A](f: ACursor => Decoder.Result[A]): Decoder[A] = new Decoder[A] {
    final def apply(c: HCursor): Decoder.Result[A] = tryDecode(c)

    override def tryDecode(c: ACursor): Decoder.Result[A] = f(c)

    override def decodeAccumulating(c: HCursor): AccumulatingDecoder.Result[A] = tryDecodeAccumulating(c)

    override def tryDecodeAccumulating(c: ACursor): AccumulatingDecoder.Result[A] = f(c) match {
      case Right(v) => Validated.valid(v)
      case Left(e) => Validated.invalidNel(e)
    }
  }

  implicit final def decodeOption[A](implicit d: Decoder[A]): Decoder[Option[A]] = withReattempt {
    case c: HCursor =>
      if (c.value.isNull) rightNone else d(c) match {
        case Right(a) => Right(Some(a))
        //removed part of the code here
        case Left(df) => Left(df)
      }
    case c: FailedCursor =>
      if (!c.incorrectFocus) rightNone else Left(DecodingFailure("[A]Option[A]", c.history))
  }

  private[this] final val rightNone: Either[DecodingFailure, Option[Nothing]] = Right(None)
}
