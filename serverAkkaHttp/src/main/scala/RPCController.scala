package wiro.server.akkaHttp

import io.circe._
import wiro.{ CustomOptionDecoder, CustomBooleanDecoder }

trait RPCController extends autowire.Server[Json, Decoder, WiroEncoder] with CustomOptionDecoder with CustomBooleanDecoder {
  def write[Result: WiroEncoder](r: Result): Json =
    implicitly[WiroEncoder[Result]].encode(r)

  //TODO handle circe error here
  def read[Result: Decoder](p: Json): Result =
    p.as[Result].right.get

  def routes: Router
  def tp: Seq[String]
  def path: String = tp.last
}
