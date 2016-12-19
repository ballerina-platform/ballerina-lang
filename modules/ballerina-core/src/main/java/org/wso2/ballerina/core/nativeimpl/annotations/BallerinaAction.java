package org.wso2.ballerina.core.nativeimpl.annotations;

import org.wso2.ballerina.core.model.types.TypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina Action.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaAction {
    String packageName() default "";
    String actionName() default "";
    String connectorName() default "";
    Argument[] args() default {};
    TypeEnum[] returnType() default {};
    BallerinaConstant[] consts() default {};
}
