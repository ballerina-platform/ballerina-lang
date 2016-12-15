package org.wso2.ballerina.core.nativeimpl.annotations;

/**
 * Represents Native Ballerina Connector.
 */
public @interface BallerinaConnector {
    String packageName() default "";
    String connectorName();
    Argument[] args() default {};
}
