package org.wso2.siddhi.extension.evalscript;

import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionEvaluationException;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionInitializationException;
import org.wso2.siddhi.extension.evalscript.exceptions.FunctionReturnTypeNotPresent;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@SiddhiExtension(namespace = "evalscript", function = "javascript")

public class EvalJavaScript implements EvalScript {

    private ScriptEngine engine;
    private Attribute.Type returnType;
    String functionName;

    public EvalJavaScript() {
        engine =  new ScriptEngineManager().getEngineByName("JavaScript");
    }

    @Override
    public void init(String name, String body) {
        this.functionName = name;
        try {
            engine.eval("function " + name + "(data)" + "{" + body + "}");
        } catch (ScriptException e) {
            throw new FunctionInitializationException("Compilation Failure of the JavaScript Function " + name, e);
        }
    }

    @Override
    public Object eval(String name, Object[] args) {
        StringBuilder jsArray = new StringBuilder("var data = [") ;
        for (int i = 0; i < args.length -1; i ++) {
            if(args[i] instanceof String) {
                jsArray.append("\"").append(args[i].toString()).append("\"");
            } else {
                jsArray.append(args[i].toString());
            }
            jsArray.append(",");
        }
        if(args[args.length-1] instanceof String) {
            jsArray.append("\"").append(args[args.length-1].toString()).append("\"");
        } else {
            jsArray.append(args[args.length-1].toString());
        }
        jsArray.append("]");
        try {
            engine.eval(jsArray.toString());
            return engine.eval(name + "(data);");
        } catch (ScriptException e) {
            throw new FunctionEvaluationException("Error evaluating JavaScript Function " + name, e);
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
