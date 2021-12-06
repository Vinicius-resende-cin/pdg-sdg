package br.ufpe.cin.soot

import br.ufpe.cin.soot.graph.{NodeType, SimpleNode, SinkNode, SourceNode}

class InvokeInstanceMethodOnFieldTest extends JSVFATest {
  override def getClassName(): String = "samples.InvokeInstanceMethodOnFieldSample"

  override def getMainMethod(): String = "m"

  override def analyze(unit: soot.Unit): NodeType = unit.getJavaSourceStartLineNumber match {
      case 16 => SourceNode
      case 18 => SinkNode
      case _ => SimpleNode
    }
}
