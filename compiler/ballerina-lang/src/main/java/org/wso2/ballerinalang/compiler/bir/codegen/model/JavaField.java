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
package org.wso2.ballerinalang.compiler.bir.codegen.model;

import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JFieldMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Represents a Java field in this implementation.
 *
 * @since 1.0.0
 */
public class JavaField {

    public JFieldMethod method;
    private final Field field;

    public JavaField(JFieldMethod method, Field field) {

        this.method = method;
        this.field = field;
    }

    public String getDeclaringClassName() {

        return field.getDeclaringClass().getName().replace(".", "/");
    }

    public String getName() {

        return field.getName();
    }

    public boolean isStatic() {

        return Modifier.isStatic(field.getModifiers());
    }

    public JFieldMethod getMethod() {

        return method;
    }

    public String getSignature() {

        return JvmCodeGenUtil.getSig(field.getType());
    }

    public Class<?> getFieldType() {

        return field.getType();
    }
}
