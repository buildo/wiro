package wiro.server.akkaHttp

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.server.Directives._

import upickle._ 
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

import wiro.models.{ RpcRequest, WiroRequest }

import scala.reflect.runtime.{universe => ru}

package object routeGenerators {
  import scala.concurrent.ExecutionContext.Implicits.global

  //Don't necessarily need the type here, it can be simplified
  trait RouteGenerator[T] extends RPCController {
    def routes: autowire.Core.Router[upickle.Js.Value]
    def tp: Seq[String]
    def buildRoute: Route = {
      (post & pathPrefix(path / Segment)) { method =>
        entity(as[WiroRequest]) { request =>
          val rpcRequest = RpcRequest(
            path = tp :+ method,
            args = request.args
          )
          complete {
            routes(
              autowire.Core.Request(
                rpcRequest.path,
                upickle.json
                  .read(rpcRequest.args)
                  .asInstanceOf[Js.Obj]
                  .value.toMap
              )
            ).map(upickle.json.write(_))
          }
        }
      }
    }
  }

  case class GeneratorBox[T: RouteGenerator](t: T) {
    def routify = implicitly[RouteGenerator[T]].buildRoute
  }

  implicit def boxer[T: RouteGenerator](t: T) = new GeneratorBox(t)
}
