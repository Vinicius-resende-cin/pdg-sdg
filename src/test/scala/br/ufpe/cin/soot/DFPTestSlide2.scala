package br.ufpe.cin.soot

import br.ufpe.cin.soot.graph.{NodeType, SimpleNode, SinkNode, SourceNode}
<<<<<<< HEAD
import br.ufpe.cin.soot.svfa.jimple.PropagateTaint
import soot.jimple.{AssignStmt, InvokeExpr, InvokeStmt}

class DFPTestSlide2(leftchangedlines: Array[Int], rightchangedlines: Array[Int]) extends JSVFATest  with PropagateTaint{
  override def getClassName(): String = "samples.SlideSample2"
  override def getMainMethod(): String = "cleanText"

  def this(){
    this(Array.empty[Int], Array.empty[Int])
=======
import br.ufpe.cin.soot.analysis.jimple.PropagateTaint
import soot.jimple.{AssignStmt, InvokeExpr, InvokeStmt}

class DFPTestSlide2(leftchangedlines: Array[Int], rightchangedlines: Array[Int], className: String, mainMethod: String) extends JSVFATest  with PropagateTaint{
  override def getClassName(): String = className
  override def getMainMethod(): String = mainMethod

  //  override def getClassName(): String = "samples.SlideSample2"
  //  override def getMainMethod(): String = "cleanText"

  def this(){
    this(Array.empty[Int], Array.empty[Int], "", "")
>>>>>>> 8627d6eb2ddcbf0f1e926b5a8f9a1b18abed9f8e
  }

  override def analyze(unit: soot.Unit): NodeType = {

    if (!leftchangedlines.isEmpty && !rightchangedlines.isEmpty){
      if (leftchangedlines.contains(unit.getJavaSourceStartLineNumber)){
        return SourceNode
      } else if (rightchangedlines.contains(unit.getJavaSourceStartLineNumber)){
        return SinkNode
      }
    }
    if(unit.isInstanceOf[InvokeStmt]) {
      val invokeStmt = unit.asInstanceOf[InvokeStmt]
      return analyzeInvokeStmt(invokeStmt.getInvokeExpr)
    }
    if(unit.isInstanceOf[soot.jimple.AssignStmt]) {
      val assignStmt = unit.asInstanceOf[AssignStmt]
      if(assignStmt.getRightOp.isInstanceOf[InvokeExpr]) {
        val invokeStmt = assignStmt.getRightOp.asInstanceOf[InvokeExpr]
        return analyzeInvokeStmt(invokeStmt)
      }
    }
    return SimpleNode
  }

  def analyzeInvokeStmt(exp: InvokeExpr) : NodeType =
    exp.getMethod.getName match {
      case "source" => SourceNode
      case "sink"   => SinkNode
      case _        => SimpleNode
    }
}

