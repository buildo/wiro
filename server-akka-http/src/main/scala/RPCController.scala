package wiro.server.akkaHttp

import io.circe.{ Decoder, Json }
import wiro.models.{ CodecsDecoder, CodecsEncoder }

trait RPCController extends autowire.Server[Json, Decoder, WiroEncoder] with CodecsDecoder with CodecsEncoder {
  def write[Result: WiroEncoder](r: Result): Json =
    implicitly[WiroEncoder[Result]].encode(r)

  //TODO handle circe error here
  def read[Result: Decoder](p: Json): Result =
    p.as[Result].right.get

  def routes: Router
  def tp: Seq[String]
  def path: String = tp.last
}
