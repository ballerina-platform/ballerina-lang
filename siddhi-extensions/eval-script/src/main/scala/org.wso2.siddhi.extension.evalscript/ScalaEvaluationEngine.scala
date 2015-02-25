package org.wso2.siddhi.extension.evalscript

import com.googlecode.scalascriptengine.EvalCode


class ScalaEvaluationEngine {
  def eval(code: String) : (Array[Any]) => Any = {
    val ect = EvalCode.withoutArgs[(Array[Any]) => Any](code)
    val f = ect.newInstance
    f()
  }
}