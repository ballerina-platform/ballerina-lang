/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina TypeConvertor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaTypeConvertor {

    /**
     * Package name of the {@code {@link org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeConvertor }}.
     * Default is "".
     *
     * @return package name of the typeConvertor.
     */
    String packageName() default "";

    /**
     * TypeConvertor name of the {@code {@link org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeConvertor }}.
     *
     * @return typeConvertor name.
     */
    String typeConverterName();

    /**
     * Argument of the typeConvertor.
     *
     * @return returns arguments of the typeConvertor.
     */
    Argument[] args() default {};

    /**
     * Return types of the typeConvertor.
     *
     * @return return types.
     */
    ReturnType[] returnType() default {};

    /**
     * Indicate Native typeConvertor is public or not.
     *
     * @return indicate native typeConvertor is public or not.
     */
    boolean isPublic() default false;

    /**
     * Constants related this typeConvertor.
     *
     * @return
     */
    BallerinaConstant[] consts() default {};
}

