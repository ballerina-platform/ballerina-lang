/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.natives.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina function.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaFunction {

    /**
     * Package name of the {@code AbstractNativeFunction}. Default is "".
     *
     * @return package name of the function.
     */
    String packageName() default "";

    /**
     * Function name of the {@code AbstractNativeFunction}.
     *
     * @return function name.
     */
    String functionName();

    /**
     * Argument of the function.
     *
     * @return returns arguments of the function.
     */
    Argument[] args() default {};

    /**
     * Return types of the function.
     *
     * @return return types.
     */
    ReturnType[] returnType() default {};

    /**
     * Indicate Native function is public or not.
     *
     * @return indicate native function is public or not.
     */
    boolean isPublic() default false;

    /**
     * Constants related this function.
     *
     * @return
     */
    BallerinaConstant[] consts() default {};
}
