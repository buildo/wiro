package wiro

import scala.reflect.runtime.{ universe => ru }

package object reflect {
  def typePath[T: ru.TypeTag]: Seq[String] = {
    val topClass = ru.typeTag[T]
    topClass.tpe.typeSymbol.fullName.toString.split('.').toSeq
  }
}
