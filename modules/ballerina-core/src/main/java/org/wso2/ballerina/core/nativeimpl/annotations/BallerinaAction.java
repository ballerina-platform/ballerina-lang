package org.wso2.ballerina.core.nativeimpl.annotations;

import org.wso2.ballerina.core.model.types.TypeEnum;

/**
 * Represents Native Ballerina Action.
 */
public @interface BallerinaAction {
    String packageName() default "";
    String actionName() default "";
    Argument[] args() default {};
    TypeEnum[] returnType() default {};
    BallerinaConstant[] consts() default {};
}
