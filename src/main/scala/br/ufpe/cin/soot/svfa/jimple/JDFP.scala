package br.ufpe.cin.soot.svfa.jimple

import br.ufpe.cin.soot.graph.{GraphNode, StatementNode}
import soot.options.Options

import soot.{PackManager}

import soot.jimple._
import soot.toolkits.graph.{ExceptionalUnitGraph}
import soot.toolkits.scalar.SimpleLocalDefs
import soot.{Local, Scene, SceneTransformer, SootMethod, Transform}

import java.util


/**
 * A Jimple based implementation of
 * Control Dependence Analysis.
 */
trait JDFP extends JSVFA with FieldSenstive with Interprocedural with PropagateTaint {

  val traversedMethodsDF = scala.collection.mutable.Set.empty[SootMethod]

  def buildDFP() { //Excluir
    buildSparseValueFlowGraph()
  }

  override def buildSparseValueFlowGraph() {
    configureSoot()
    beforeGraphConstruction()
    val (pack1, t1) = createSceneTransform()
    val (pack2, t2) = createSceneTransformDFP()

    PackManager.v().getPack(pack1).add(t1)
    PackManager.v().getPack(pack2).add(t2)

    configurePackages().foreach(p => PackManager.v().getPack(p).apply())

    afterGraphConstruction()
  }

  def createSceneTransformDFP(): (String, Transform) = ("wjtp", new Transform("wjtp.dfp", new TransformerDFP()))

  class TransformerDFP extends SceneTransformer {
    override def internalTransform(phaseName: String, options: util.Map[String, String]): scala.Unit = {
      pointsToAnalysis = Scene.v().getPointsToAnalysis
      Scene.v().getEntryPoints.forEach(method => {
        traverseDFP(method)
        methods = methods + 1
      })
    }
  }



  def traverseDFP(method: SootMethod, forceNewTraversal: Boolean = false) : scala.Unit = {
    if((!forceNewTraversal) && (method.isPhantom || traversedMethodsDF.contains(method))) {
      return
    }

    traversedMethodsDF.add(method)

    val body  = method.retrieveActiveBody()
    val graph = new ExceptionalUnitGraph(body)
    val defs  = new SimpleLocalDefs(graph)

    body.getUnits.forEach(unit => {
      try{
        val v = Statement.convert(unit)

        v match {
          case IfStmt(base) => traverse(IfStmt(base), method, defs) //if statment
          case ReturnStmt(base) => traverse(ReturnStmt(base), method, defs) //return
          case _ =>
        }

      }catch {
        case e: Exception => return
      }
    })

  }

  case class IfStmt(b: soot.Unit) extends Statement(b) {
    val stmt = base.asInstanceOf[soot.jimple.IfStmt]
  }

  object Statement {
    def convert(base: soot.Unit): Statement =
      if(base.isInstanceOf[soot.jimple.AssignStmt]) {
        AssignStmt(base)
      }
      else if(base.isInstanceOf[soot.jimple.InvokeStmt]) {
        InvokeStmt(base)
      }else if(base.isInstanceOf[soot.jimple.IfStmt]) {
        IfStmt(base)
      }else if(base.isInstanceOf[soot.jimple.ReturnStmt]) {
        ReturnStmt(base)
      }
      else InvalidStmt(base)
  }

  def traverse(stmt: IfStmt, method: SootMethod, defs: SimpleLocalDefs) : scala.Unit = {
    addEdgesFromIfStmt(stmt.base, method, defs)
  }

  def addEdgesFromIfStmt(sourceStmt: soot.Unit, method: SootMethod, defs: SimpleLocalDefs) = {

    //Add useBoxes used in if statement
    sourceStmt.getUseAndDefBoxes.forEach(useBox => {
      if (useBox.getValue.isInstanceOf[Local]) {
        val local = useBox.getValue.asInstanceOf[soot.Local]
        copyRule(sourceStmt, local, method, defs)
      }
    })

  }

  case class ReturnStmt(b: soot.Unit) extends Statement(b) {
    val stmt = base.asInstanceOf[soot.jimple.ReturnStmt]
  }


  def traverse(stmt: ReturnStmt, method: SootMethod, defs: SimpleLocalDefs) : scala.Unit = {
    val op = stmt.stmt.getUseBoxes

    op.forEach(useBox => {
      (useBox.getValue) match {
        case (q: InstanceFieldRef) => loadRule(stmt.stmt, q, method, defs)
        case (q: ArrayRef) => loadArrayRule(stmt.stmt, q, method, defs)
        case (q: Local) => copyRule(stmt.stmt, q, method, defs)
        case _ =>
      }
    })
  }

//  def copyRule(targetStmt: soot.Unit, local: Local, method: SootMethod, defs: SimpleLocalDefs) = {
//    defs.getDefsOfAt(local, targetStmt).forEach(sourceStmt => {
//      val source = createNode(method, sourceStmt)
//      val target = createNode(method, targetStmt)
//      updateGraph(source, target)
//    })
//  }

//  def loadRule(stmt: soot.Unit, ref: InstanceFieldRef, method: SootMethod, defs: SimpleLocalDefs) : scala.Unit =
//  {
//    val base = ref.getBase
//    // value field of a string.
//    val className = ref.getFieldRef.declaringClass().getName
//    if ((className == "java.lang.String") && ref.getFieldRef.name == "value") {
//      if (base.isInstanceOf[Local]) {
//        defs.getDefsOfAt(base.asInstanceOf[Local], stmt).forEach(source => {
//          val sourceNode = createNode(method, source)
//          val targetNode = createNode(method, stmt)
//          updateGraph(sourceNode, targetNode)
//        })
//      }
//      return;
//    }
//  }

//  def loadArrayRule(targetStmt: soot.Unit, ref: ArrayRef, method: SootMethod, defs: SimpleLocalDefs) : scala.Unit = {
//    val base = ref.getBase
//
//    if(base.isInstanceOf[Local]) {
//      val local = base.asInstanceOf[Local]
//
//      defs.getDefsOfAt(local, targetStmt).forEach(sourceStmt => {
//        val source = createNode(method, sourceStmt)
//        val target = createNode(method, targetStmt)
//        updateGraph(source, target)
//      })
//
//      val stores = arrayStores.getOrElseUpdate(local, List())
//      stores.foreach(sourceStmt => {
//        val source = createNode(method, sourceStmt)
//        val target = createNode(method, targetStmt)
//        updateGraph(source, target)
//      })
//    }
//  }

}
