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
package org.ballerinalang.natives.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina TypeMapper.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaTypeMapper {

    /**
     * Package name of the {@code AbstractNativeTypeMapper}.
     * Default is "".
     *
     * @return package name of the typeMapper.
     */
    String packageName() default "";

    /**
     * TypeMapper name of the {@code AbstractNativeTypeMapper}.
     *
     * @return typeMapper name.
     */
    String typeMapperName();

    /**
     * Argument of the typeMapper.
     *
     * @return returns arguments of the typeMapper.
     */
    Argument[] args() default {};

    /**
     * Return types of the typeMapper.
     *
     * @return return types.
     */
    ReturnType[] returnType() default {};

    /**
     * Indicate Native typeMapper is public or not.
     *
     * @return indicate native typeMapper is public or not.
     */
    boolean isPublic() default false;

    /**
     * Constants related this typeMapper.
     *
     * @return
     */
    BallerinaConstant[] consts() default {};
}

