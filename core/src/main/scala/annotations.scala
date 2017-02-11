package wiro

package object annotation {
  import scala.reflect.macros.Context
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly

  class token extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro tokenMacro.impl
  }

  class command extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro commandMacro.impl
  }

  class query extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro queryMacro.impl
  }

  object tokenMacro {
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
            case innerArgs => q"val actionCommand: String" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val actionCommand: String" +: innerArgs
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
            case innerArgs => q"val actionQuery: String" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val allArgs = args.map{
            case innerArgs => q"val actionQuery: String" +: innerArgs
          }
          q"$annots def $methodName(...$allArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }

}
