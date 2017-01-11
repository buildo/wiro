package wiro.client.akkaHttp

import upickle._

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import wiro.models.RpcRequest
import wiro.models.ClientConfig

class WiroClient(
  conf: ClientConfig,
  actorSystem: ActorSystem,
  materializer: ActorMaterializer,
  path: String = "rpc"
) extends autowire.Client[Js.Value, upickle.default.Reader, upickle.default.Writer]{
  import de.heikoseeberger.akkahttpcirce.CirceSupport._
  import io.circe.generic.auto._

  private[this] implicit val s = actorSystem
  private[this] implicit val m = materializer
  implicit val executionContext = s.dispatcher

  case class ClientRpcRequest(path: Seq[String] , args: String)

  private[this] val url = s"http://${conf.host}:${conf.port}/${path}"

  override def doCall(req: Request): Future[Js.Value] = {
    val rpcRequest = ClientRpcRequest(
      path = req.path,
      args= upickle.default.write(req.args)
    )

    for {
      body <- Marshal(rpcRequest).to[RequestEntity]
      response <- Http().singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = url,
        entity = body
      ))
      result <- Unmarshal(response).to[String]
    } yield (json.read(result))
  }

  def read[Result: upickle.default.Reader](p: Js.Value) = {
    default.readJs[Result](p)
  }

  def write[Result: upickle.default.Writer](r: Result) =
    default.writeJs(r)
}
