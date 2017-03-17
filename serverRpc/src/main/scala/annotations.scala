package wiro.annotation
package rpc

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

class rpc extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro commandMacro.impl
}
