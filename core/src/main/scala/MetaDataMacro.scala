package wiro

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import wiro.annotation._

sealed trait OperationType {
  def name: Option[String]
}

sealed trait AuthenticationType
object AuthenticationType {
  case object Nope extends AuthenticationType
  case object Token extends AuthenticationType
}

object OperationType {
  case class Command(name: Option[String]) extends OperationType
  case class Query(name: Option[String]) extends OperationType
}

case class MethodMetaData(
  operationType: OperationType,
  authenticationType: AuthenticationType
)

trait MetaDataMacro {
  def deriveMetaData[A]: Map[String, MethodMetaData] = macro MetaDataMacro.deriveMetaDataImpl[A]
}

object MetaDataMacro extends MetaDataMacro {
  def deriveMetaDataImpl[A: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._

    val decls =  weakTypeOf[A].decls.collect {
      case m: MethodSymbol =>
        val methodName = m.fullName
        val operationType = m.annotations.collectFirst {
          case opAnnotation if opAnnotation.tpe <:< c.weakTypeOf[command] =>
            val name = opAnnotation.tree.children.tail.head
            q"OperationType.Command($name)"
          case opAnnotation if opAnnotation.tpe <:< c.weakTypeOf[query] =>
            val name = opAnnotation.tree.children.tail.head
            q"OperationType.Query($name)"
        }

        val authenticationType = m.annotations.collectFirst {
          case authAnnotation if authAnnotation.tpe <:< c.weakTypeOf[auth] =>
            authAnnotation.tree.children.tail.head
        }.headOption match {
          case Some(tree) => tree
          case None => q"AuthenticationType.Nope"
        }
        q"($methodName -> $operationType.map { o => MethodMetaData(o, $authenticationType) })"
    }

    q"Map(..$decls) collect { case (k, Some(v)) => (k -> v) }"
  }
}
