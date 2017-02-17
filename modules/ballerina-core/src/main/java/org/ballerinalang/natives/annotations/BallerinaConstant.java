/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.natives.annotations;

import org.ballerinalang.model.types.TypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Native Ballerina Constant Field.
 *
 * const typeName Identifier '=' literalValue;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BallerinaConstant {

    /**
     * Identifier of the field.
     *
     * @return identifier.
     */
    String identifier();

    /**
     * Type of the Ballerina type.
     *
     * @return Ballerina Type Classname.
     */
    TypeEnum type();

    /**
     * Constant value.
     *
     * @return value as a String.
     */
    String value();

    /**
     * Documentation Annotation where this Constant apply.
     *
     * @return defined scopes.
     */
    String[] argumentRefs() default {};
}
