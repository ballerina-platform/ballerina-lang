package org.wso2.siddhi.core.util.extension.holder;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.function.EvalScript;

public class EvalScriptExtensionHolder extends AbstractExtensionHolder {

    private static EvalScriptExtensionHolder instance;

    protected EvalScriptExtensionHolder(ExecutionPlanContext executionPlanContext) {
        super(EvalScript.class, executionPlanContext);
    }

    public static EvalScriptExtensionHolder getInstance(ExecutionPlanContext executionPlanContext) {
        if (instance == null) {
            instance = new EvalScriptExtensionHolder(executionPlanContext);
        }
        return instance;
    }
}
