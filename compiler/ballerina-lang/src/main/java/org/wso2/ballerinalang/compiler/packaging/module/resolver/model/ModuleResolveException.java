package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

public class ModuleResolveException extends RuntimeException {
    public ModuleResolveException(String message) {
        super(message);
    }

    public ModuleResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
