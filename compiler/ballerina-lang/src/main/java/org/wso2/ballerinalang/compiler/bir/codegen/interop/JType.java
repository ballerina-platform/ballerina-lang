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

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

public class JType extends BType {

    public String type;

    public JType(String type) {
        super(JTypeTags.JTYPE, null);
        this.type = type;
    }

    static final String JBYTE_KIND = "byte";
    static final String JCHAR_KIND = "char";
    static final String JSHORT_KIND = "short";
    static final String JINT_KIND = "int";
    static final String JLONG_KIND = "long";
    static final String JFLOAT_KIND = "float";
    static final String JDOUBLE_KIND = "double";
    static final String JBOOLEAN_KIND = "boolean";
    static final String JVOID_KIND = "void";
    static final String JARRAY_KIND = "array";
    static final String JREF_KIND = "ref";

    static JType jByte = new JType(JBYTE_KIND);
    static JType jChar = new JType(JCHAR_KIND);
    static JType jShort = new JType(JSHORT_KIND);
    static JType jInt = new JType(JINT_KIND);
    static JType jLong = new JType(JLONG_KIND);
    static JType jFloat = new JType(JFLOAT_KIND);
    static JType jDouble = new JType(JDOUBLE_KIND);
    static JType jBoolean = new JType(JBOOLEAN_KIND);
    static JType jVoid = new JType(JVOID_KIND);

    public static class JArrayType extends JType {
        JType elementType;

        public JArrayType(JType elementType) {
            super(JARRAY_KIND);
            this.elementType = elementType;
        }
    }

    public static class JRefType extends JType {
        String type;
        boolean isInterface = false;
        boolean isArray = false;

        public JRefType(String type) {
            super(JREF_KIND);
            this.type = type;
        }
    }

    static JType getJTypeFromTypeName(String typeName) {

        switch (typeName) {
            case JBYTE_KIND:
                return jByte;
            case JCHAR_KIND:
                return jChar;
            case JSHORT_KIND:
                return jShort;
            case JINT_KIND:
                return jInt;
            case JLONG_KIND:
                return jLong;
            case JFLOAT_KIND:
                return jFloat;
            case JDOUBLE_KIND:
                return jDouble;
            case JBOOLEAN_KIND:
                return jBoolean;
            default:
                return new JRefType(typeName);
        }
    }

    static JArrayType getJArrayTypeFromTypeName(String typeName, byte dimensions) {

        JArrayType arrayType = new JArrayType(getJTypeFromTypeName(typeName));
        int i = 1;
        while (i < ((int) dimensions)) {
            arrayType = new JArrayType(arrayType);
            i += 1;
        }

        return arrayType;
    }
}
