package wiro.client.akkaHttp

import cats.data.{ NonEmptyList, ValidatedNel }
import cats.syntax.either._

import io.circe.{ Decoder, DecodingFailure, Json }

object FailSupport {
  implicit def wiroCanFailDecoder[T: Decoder, A: Decoder] = new WiroDecoder[Either[T, A]] {
    private[this] def decodeLeft(j: Json): Decoder.Result[Left[T, A]] = j.as[T].map(Left.apply)
    private[this] def decodeRight(j: Json): Decoder.Result[Right[T, A]] = j.as[A].map(Right.apply)

    private[this] def decodeEither(j: Json): ValidatedNel[DecodingFailure, Either[T, A]] =
      decodeRight(j).toValidatedNel findValid decodeLeft(j).toValidatedNel
    private[this] def errorMessage(errorList: NonEmptyList[DecodingFailure]) =
      errorList.map(_.getMessage).toList.mkString

    def decode(j: Json): Either[T, A] = decodeEither(j).fold(
      error => throw new Exception(errorMessage(error)), identity
    )
  }
}
