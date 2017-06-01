import scala.concurrent.Future

// Models definition
object models {
  case class User(name: String)
}

object controllers {
  import models._
  import wiro.annotation._

  // Error and success messages
  case class Error(msg: String)
  case class Ok(mgs: String)

  // API interface
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
