package wiro.server.akkaHttp

import io.circe._
import wiro.{ CustomOptionDecoder, CustomBooleanDecoder }

import cats.syntax.either._

trait RPCServer extends autowire.Server[Json, Decoder, WiroEncoder] with CustomOptionDecoder with CustomBooleanDecoder {
  def write[Result: WiroEncoder](r: Result): Json =
    implicitly[WiroEncoder[Result]].encode(r)

  def read[Result: Decoder](p: Json): Result =
    p.as[Result].toTry.get

  def routes: Router
}
