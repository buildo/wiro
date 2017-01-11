package wiro.apps

import wiro.models.ClientConfig
//import wiro.client.akkaHttp._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Client extends App {
  import interface._

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // val doghouse = new WiroClient(
  //   conf = ClientConfig("localhost", 8080),
  //   actorSystem = actorSystem,
  //   materializer = materializer
  // )[DoghouseApi]
}
