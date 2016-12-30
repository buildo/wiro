package wiro.apps

import wiro.models.ClientConfig
import wiro.client.akkaHttp._

import autowire._

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Client extends App {
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import actorSystem.dispatcher

  val doghouse = new WiroClient(
    conf = ClientConfig("localhost", 8080),
    actorSystem = actorSystem,
    materializer = materializer
  )[DoghouseApi]

  val futureDog: Future[Dog] = doghouse.getPuppy("a").call() recover {
    case e: Exception => e.printStackTrace; throw e
  }

  futureDog map (println)
}
