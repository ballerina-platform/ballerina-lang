package org.wso2.siddhi.extension.evalscript.exceptions;

import javax.script.ScriptException;

public class FunctionEvaluationException extends RuntimeException {
    public FunctionEvaluationException(String message) {
        super(message);
    }

    public FunctionEvaluationException(String message, Exception e) {
        super(message,e);
    }
}
