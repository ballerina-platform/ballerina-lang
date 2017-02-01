package org.wso2.ballerina.tooling.service.workspace.swagger.api;

/**
 * This is API exception class specific to this service.
 */
public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }
}
