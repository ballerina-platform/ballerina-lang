/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Represents a Java field in this implementation.
 *
 * @since 1.0.0
 */
class JavaField {
    JFieldMethod method;
    private Field field;

    JavaField(JFieldMethod method, Field field) {
        this.method = method;
        this.field = field;
    }

    String getDeclaringClassName() {
        return field.getDeclaringClass().getName();
    }

    String getName() {
        return field.getName();
    }

    boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    JFieldMethod getMethod() {
        return method;
    }

    String getSignature() {
        return JInterop.getSig(field.getType());
    }

    Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * This enum is used to indicate whether the given Java field is a static field or an instance field.
     *
     * @since 1.0.0
     */
    enum JFieldKind {
        STATIC("static"),
        INSTANCE("instance");

        private String strValue;

        JFieldKind(String strValue) {
            this.strValue = strValue;
        }

        static JFieldKind getKind(String value) {
            if ("static".equals(value)) {
                return STATIC;
            }
            return INSTANCE;
        }

        String getStringValue() {
            return this.strValue;
        }
    }

    /**
     * This enum is used to indicate whether the given Ballerina function mutates or access the java field.
     *
     * @since 1.0.0
     */
    enum JFieldMethod {
        ACCESS("access"),
        MUTATE("mutate");

        private String strValue;

        JFieldMethod(String strValue) {
            this.strValue = strValue;
        }

        static JFieldMethod getKind(String value) {
            if ("access".equals(value)) {
                return ACCESS;
            }
            return MUTATE;
        }

        String getStringValue() {
            return this.strValue;
        }
    }
}
