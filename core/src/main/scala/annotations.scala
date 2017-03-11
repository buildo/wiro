package wiro

package object annotation {
  import scala.reflect.macros.Context
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly

  def chooseOperationName(c: Context, methodName: String) = {
    import c.universe._
    c.prefix.tree match {
      case q"new $annot(name = $s)" => c.eval[String](c.Expr(s))
      case q"new $annot($s)" => c.eval[String](c.Expr(s))
      case q"new $annot" => methodName
      case _ => c.abort(c.enclosingPosition, s"\n\nMissing annotation @<annot>(<name>)\n")
    }
  }

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

  object commandMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      val commandValDef: ValDef = q"val actionCommand: String"

      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val commandArgs = args.map(commandValDef +: _)
          val operationName = c.universe.TermName(
            chooseOperationName(c, methodName.toString)
          )
          q"$annots def $operationName(...$commandArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val commandArgs = args.map(commandValDef +: _)
          val operationName = c.universe.TermName(
            chooseOperationName(c, methodName.toString)
          )
          q"$annots def $operationName(...$commandArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }

  object queryMacro {
    def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
      import c.universe._

      val queryValDef: ValDef = q"val actionQuery: String"

      val result = annottees.map(_.tree).toList match {
        case q"$annots def $methodName(...$args): $tpe = $body" :: nil =>
          val queryArgs = args.map(queryValDef +: _)
          val operationName = c.universe.TermName(
            chooseOperationName(c, methodName.toString)
          )
          q"$annots def $operationName(...$queryArgs): $tpe = $body"
        case q"$annots def $methodName(...$args): $tpe" :: nil =>
          val queryArgs = args.map(queryValDef +: _)
          val operationName = c.universe.TermName(
            chooseOperationName(c, methodName.toString)
          )
          q"$annots def $operationName(...$queryArgs): $tpe"
      }
      c.Expr[Any](result)
    }
  }

}
