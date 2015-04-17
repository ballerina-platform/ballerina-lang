package org.wso2.siddhi.core.util.parser;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.extension.holder.EvalScriptExtensionHolder;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.extension.Extension;

public class FunctionParser {
    static final Logger log = Logger.getLogger(FunctionParser.class);

    public static void addFunction(SiddhiContext siddhiContext, final FunctionDefinition functionDefinition) {
        ExecutionPlanContext executionPlanContext = new ExecutionPlanContext();
        executionPlanContext.setSiddhiContext(siddhiContext);
        EvalScript evalScript = (EvalScript) SiddhiClassLoader.loadExtensionImplementation(
                new Extension() {
                    @Override
                    public String getNamespace() {
                        return "evalscript";
                    }

                    @Override
                    public String getFunction() {
                        return functionDefinition.getLanguage().toLowerCase();
                    }
                }, EvalScriptExtensionHolder.getInstance(executionPlanContext));
        evalScript.init(functionDefinition.getFunctionID(), functionDefinition.getBody());
        evalScript.setReturnType(functionDefinition.getReturnType());
        siddhiContext.getScriptFunctionMap().put(functionDefinition.getFunctionID(),evalScript);
    }
}