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

  def read[Result: Decoder](p: Json): Result = {
    //This trick is required to match the result type of autowire
    val right = Json.obj("Right" -> Json.obj("b" -> p))
    val left = Json.obj("Left" -> Json.obj("a" -> p))
    (left.as[Result], right.as[Result]) match {
      case (_, Right(result)) => result
      case (Right(result), _) => result
      case (Left(error1), Left(error2))  =>
        throw new Exception(error1.getMessage + error2.getMessage)
    }
  }

  override def doCall(autowireRequest: Request): Future[Json] =
    Http().singleRequest(requestBuilder.build(autowireRequest.path, autowireRequest.args))
      .flatMap{ response => Unmarshal(response.entity).to[Json] }
}
