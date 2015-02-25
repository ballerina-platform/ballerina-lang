package org.wso2.siddhi.extension.evalscript;

import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionEvaluationException;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionInitializationException;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionReturnTypeNotPresent;
import org.wso2.siddhi.query.api.definition.Attribute;
import scala.Function1;

public class EvalScala implements EvalScript {

    private Function1<Object[], Object> scalaFunction;
    private Attribute.Type returnType;
    private String functionName;

    public EvalScala() {

    }

    public void init(String name, String body) {
        this.functionName = name;
        ScalaEvaluationEngine scalaEvaluationEngine = new ScalaEvaluationEngine();
        try {
            scalaFunction = scalaEvaluationEngine.eval("data: (Array[Any]) =>  {\n" + body + "\n}");
        } catch (Exception e) {
            throw new FunctionInitializationException("Compilation Failure of the Scala Function " + name, e);
        }
    }

    @Override
    public Object eval(String name, Object[] arg) {
        try {
            return scalaFunction.apply(arg);
        } catch (Exception e) {
            throw new FunctionEvaluationException("Error while evaluating function " + name, e);
        }
    }

    @Override
    public void setReturnType(Attribute.Type returnType) {
        if( returnType == null ) {
            throw new FunctionReturnTypeNotPresent("Cannot find the return type of the function " + functionName);
        }
        this.returnType = returnType;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}