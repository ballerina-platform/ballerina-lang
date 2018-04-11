/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.compiler.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@code SupportEndpointTypes} annotation represents an array of
 * support Ballerina endpoint names.
 *
 * @since 0.965.0
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupportEndpointTypes {

    /**
     * Returns an array of supported Ballerina endpoints names.
     *
     * @return an array of supported Ballerina endpoints names
     */
    EndpointType[] value() default {};


    /**
     * Represents endpoint type.
     *
     * @since 0.965.0
     */
    public static @interface EndpointType {

        /**
         * Name of the endpoint type.
         *
         * @return name of the endpoint
         */
        String name();

        /**
         * Name of the endpoint package.
         *
         * @return name of the endpoint package
         */
        String packageName();

        /**
         * Name of the endpoint package organization.
         *
         * @return name of the endpoint package organization
         */
        String orgName() default "";
    }
}
