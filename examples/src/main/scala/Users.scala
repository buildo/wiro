import scala.concurrent.Future

object models {
  case class User(name: String)
}

object controllers {
  import models._
  import wiro.annotation._

  case class Error(msg: String)
  case class Ok(mgs: String)

  @path("users")
  trait UsersApi {

    @query(name = Some("getUser"))
    def getPuppy(
      id: Int
    ): Future[Either[Error, Ok]]

    @command(name = Some("addUser"))
    def getPuppy(
      id: Int,
      name: String
    ): Future[Either[Error, User]]
  }
}
