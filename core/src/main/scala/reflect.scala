package wiro

import scala.reflect.runtime.{ universe => ru }

package object reflect {
  def typePath[T: ru.TypeTag]: Seq[String] = {
    val topClass = ru.typeTag[T]
    topClass.tpe.typeSymbol.fullName.toString.split('.').toSeq
  }
}

package object annotation {
  import scala.reflect.macros.Context
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly

  class auth extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro authMacro.impl
  }

  class command extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro commandMacro.impl
  }

  class query extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro commandMacro.impl
  }

  object authMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._
      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val token: String" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val token: String" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }

  object commandMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._
      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val action: Command" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val action: Command" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }

  object queryMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._
      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val action: Query" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val action: Query" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }
}
