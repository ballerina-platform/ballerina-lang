package org.ballerinalang.natives.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina Connector.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaConnector {
    String packageName() default "";
    String connectorName();
    Argument[] args() default {};
}
