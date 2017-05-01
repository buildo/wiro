package wiro

package object annotation {
  import scala.reflect.macros.Context
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly

  class token extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro tokenMacro.impl
  }

  object tokenMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      val tokenValDef: ValDef = q"val token: String"

      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val tokenArgs = args.map(tokenValDef +: _)
          q"$annots def $methodName(...$tokenArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val tokenArgs = args.map(tokenValDef +: _)
          q"$annots def $methodName(...$tokenArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }
}
