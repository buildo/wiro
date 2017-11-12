package wiro
package client.akkaHttp

import akka.actor.ActorSystem

import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal

import akka.stream.ActorMaterializer

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import io.circe._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

trait RPCClientContext[T] extends MetaDataMacro with PathMacro {
  def methodsMetaData: Map[String, MethodMetaData]
  def tp: Seq[String]
  def path: String = tp.last
}

class RPCClient(
  config: Config,
  ctx: RPCClientContext[_]
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  executionContext: ExecutionContext
) extends autowire.Client[Json, WiroDecoder, Encoder] {
  private[wiro] val requestBuilder = new RequestBuilder(config, ctx)

  def write[Result: Encoder](r: Result): Json = r.asJson

  def read[Result: WiroDecoder](p: Json): Result =
    implicitly[WiroDecoder[Result]].decode(p)

  override def doCall(autowireRequest: Request): Future[Json] =
    Http().singleRequest(requestBuilder.build(autowireRequest.path, autowireRequest.args))
      .flatMap{ response => Unmarshal(response.entity).to[Json] }
}
