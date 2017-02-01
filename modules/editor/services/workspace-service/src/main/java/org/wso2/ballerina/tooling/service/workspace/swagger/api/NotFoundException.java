package org.wso2.ballerina.tooling.service.workspace.swagger.api;

/**
 * Not found exception class implemented for this service.
 */
public class NotFoundException extends ApiException {
    private int code;
    public NotFoundException (int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
