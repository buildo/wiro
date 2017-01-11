package wiro

import scala.reflect.runtime.{ universe => ru }

package object reflect {
  def typePath[T: ru.TypeTag]: Seq[String] = {
    val topClass = ru.typeTag[T]
    topClass.tpe.typeSymbol.fullName.toString.split('.').toSeq
  }
}

package object annotation {
  import scala.annotation.compileTimeOnly
  import scala.meta._

  class auth extends scala.annotation.StaticAnnotation {

  inline def apply(d: Any): Any = meta {
    def userParam = Term.Param(
      mods = Nil,
      name = Term.Name("token"),
      decltpe = Some(Type.Name("String")),
      default = None
    )

    d match {
      case defn: Defn.Def =>
        val paramss = defn.paramss.map(userParam +: _)
        defn.copy(paramss = paramss)
      case q"def $name(...$params): $tpe" =>
        val declParams = params.map(userParam +: _)
        q"def $name(...$declParams): $tpe"
      case a =>
        println(a)
        abort("@auth must annotate a def")
    }
  }
}
}
