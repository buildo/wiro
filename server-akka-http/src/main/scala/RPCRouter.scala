package wiro.server.akkaHttp

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route

import upickle._ 
import wiro.models.RpcRequest

trait Router {
  def route: Route
}

class RPCRouterImpl(
  apiImpl: AutowireRPCServer,
  rpcPath: String
)(implicit
  executionContext: ExecutionContext
) extends Router {
  import de.heikoseeberger.akkahttpcirce.CirceSupport._
  import io.circe.generic.auto._

  override val route: Route = {
    (post & path("rpc") & entity(as[RpcRequest])) { request =>
      complete {
        apiImpl.routes(
          autowire.Core.Request(
            request.path,
            upickle.json
              .read(request.args)
              .asInstanceOf[Js.Obj]
              .value.toMap
          )
        ).map(upickle.json.write(_))
      }
    }
  }
}
