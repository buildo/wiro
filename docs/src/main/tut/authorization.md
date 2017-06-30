## Authorization

How do I authorize my routes? just add a `Token` argument to your methods.


Wiro takes care of extracting the token from "Authorization" http header in the form `Token token=${YOUR_TOKEN}`. The token will automagically passed as `token` argument.

For example,
```tut:silent
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import scala.concurrent.Future
import wiro.Token
import wiro.annotation._
import wiro.server.akkaHttp._

case class Unauthorized(msg: String)
implicit def unauthorizedToResponse = new ToHttpResponse[Unauthorized] {
  def response(error: Unauthorized) = HttpResponse(
    status = StatusCodes.Unauthorized,
    entity = "canna cross it"
  )
}

@path("users")
trait UsersApi {

  @query
  def getUser(
    token: Token,
    id: Int
  ): Future[Either[Unauthorized, String]]

  @command
  def insertString(
    token: Token,
    id: Int,
    name: String
  ): Future[Either[Unauthorized, String]]
}
```
