package wiro

import io.circe._

//This code is modified from circe (https://github.com/circe/circe).
//Circe is licensed under http://www.apache.org/licenses/LICENSE-2.0
//with the following notice https://github.com/circe/circe/blob/master/NOTICE.
trait CustomOptionDecoder {
  implicit final def decodeOption[A](implicit d: Decoder[A]): Decoder[Option[A]] = Decoder.withReattempt {
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
