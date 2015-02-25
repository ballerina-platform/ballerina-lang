package org.wso2.siddhi.extension.evalscript.exceptions;

public class FunctionInitializationException extends RuntimeException {
    public FunctionInitializationException(String message, Exception e) {
        super(message);
    }
}
