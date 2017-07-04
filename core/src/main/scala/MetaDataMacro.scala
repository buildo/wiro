package wiro

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import wiro.annotation._

sealed trait OperationType {
  def name: Option[String]
}

object OperationType {
  case class Command(name: Option[String]) extends OperationType
  case class Query(name: Option[String]) extends OperationType
}

case class MethodMetaData(
  operationType: OperationType
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
          case opAnnotation if opAnnotation.tree.tpe <:< c.weakTypeOf[command] =>
            val name = opAnnotation.tree.children.tail.head
            q"OperationType.Command($name)"
          case opAnnotation if opAnnotation.tree.tpe <:< c.weakTypeOf[query] =>
            val name = opAnnotation.tree.children.tail.head
            q"OperationType.Query($name)"
        }

        q"($methodName -> $operationType.map { o => MethodMetaData(o) })"
    }

    q"Map(..$decls) collect { case (k, Some(v)) => (k -> v) }"
  }
}
