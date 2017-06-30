## Authorization

How do I authorize my routes? just add a `Token` argument to your methods.

For example,
```tut
import wiro.Token

// API interface
@path("users")
trait UsersApi {

  @query
  def getUser(
    token: Token,
    id: Int
  ): Future[Either[Unauthorized, User]]

  @command
  def insertUser(
    token: Token,
    id: Int,
    name: String
  ): Future[Either[Unauthorized, User]]
}
```

Wiro takes care of extracting the token from "Authorization" http header in the form `Token token=${YOUR_TOKEN}`. The extracted token will automagically passed as `token` argument.
