package org.wso2.siddhi.core.executor.function;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.query.api.definition.Attribute;

public class ScriptFunctionExecutor extends FunctionExecutor {

    static final Logger log = Logger.getLogger(ScriptFunctionExecutor.class);

    private String functionId;
    Attribute.Type returnType;
    EvalScript evalScript;

    public ScriptFunctionExecutor(String name) {
        this.functionId = name;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        returnType = executionPlanContext.getSiddhiContext().getEvalScript(functionId).getReturnType();
        evalScript = executionPlanContext.getSiddhiContext().getEvalScript(functionId);
    }

    @Override
    protected Object execute(Object[] data) {
        return evalScript.eval(functionId,(Object[])data);
    }

    @Override
    protected Object execute(Object data) {
        return evalScript.eval(functionId, new Object[]{data});
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
