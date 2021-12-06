package br.ufpe.cin.soot

import br.ufpe.cin.soot.graph.{NodeType, SimpleNode, SinkNode, SourceNode}

class HashmapTest extends JSVFATest {
  override def getClassName(): String = "samples.HashmapSample"

  override def getMainMethod(): String = "m"

  override def analyze(unit: soot.Unit): NodeType =
    unit.getJavaSourceStartColumnNumber match {
        case 11 => SourceNode
        case 12 => SinkNode
        case _ => SimpleNode
    }
}
