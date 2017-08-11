/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.lang.annotation.Repeatable;

/**
 * Represents a custom Ballerina Annotation.
 */
@Repeatable(value = BallerinaAnnotations.class)
public @interface BallerinaAnnotation {

    /**
     * The name of the Ballerina package this annotation is from. Default is "".
     *
     * @return package name of the annotation.
     */
    String packageName() default "";

    /**
     * Name of the annotation.
     *
     * @return annotation name.
     */
    String annotationName();

    /**
     * Attributes of this annotation.
     *
     * @return returns attributes of the annotation.
     */
    Attribute[] attributes();

}
