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

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JBYTE;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JCHAR;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JDOUBLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JFLOAT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JINT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JLONG;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JREF;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JSHORT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JVOID;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BYTE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.INT;

/**
 * Interop representation of Java types.
 *
 * @since 1.2.0
 */
public class JType extends BType {

    private static final String JBYTE_KIND = "byte";
    private static final String JCHAR_KIND = "char";
    private static final String JSHORT_KIND = "short";
    private static final String JINT_KIND = "int";
    private static final String JLONG_KIND = "long";
    private static final String JFLOAT_KIND = "float";
    private static final String JDOUBLE_KIND = "double";
    private static final String JBOOLEAN_KIND = "boolean";
    private static final String JVOID_KIND = "void";
    private static final String JARRAY_KIND = "array";
    private static final String JREF_KIND = "ref";
    private static final String JNO_KIND = "no";
    public static JType jVoid = new JType(JVOID);
    private static JType jByte = new JType(JBYTE);
    private static JType jChar = new JType(JCHAR);
    private static JType jShort = new JType(JSHORT);
    private static JType jInt = new JType(JINT);
    private static JType jLong = new JType(JLONG);
    private static JType jFloat = new JType(JFLOAT);
    private static JType jDouble = new JType(JDOUBLE);
    private static JType jBoolean = new JType(JBOOLEAN);
    public int jTag;

    JType(int jTag) {

        super(JTypeTags.JTYPE, null);
        this.jTag = jTag;
    }

    public static JType getJTypeFromTypeName(String typeName) {

        return switch (typeName) {
            case JBYTE_KIND -> jByte;
            case JCHAR_KIND -> jChar;
            case JSHORT_KIND -> jShort;
            case JINT_KIND -> jInt;
            case JLONG_KIND -> jLong;
            case JFLOAT_KIND -> jFloat;
            case JDOUBLE_KIND -> jDouble;
            case JBOOLEAN_KIND -> jBoolean;
            case JVOID_KIND -> jVoid;
            default -> new JRefType(typeName.replace('.', '/'));
        };
    }

    public static JType getJTypeForPrimitive(String typeName) {

        return switch (typeName) {
            case JBYTE_KIND -> jByte;
            case JCHAR_KIND -> jChar;
            case JSHORT_KIND -> jShort;
            case JINT_KIND -> jInt;
            case JLONG_KIND -> jLong;
            case JFLOAT_KIND -> jFloat;
            case JDOUBLE_KIND -> jDouble;
            case JBOOLEAN_KIND -> jBoolean;
            case JVOID_KIND -> jVoid;
            default -> throw new IllegalArgumentException("The Java " + typeName + " type is not yet supported.");
        };
    }

    public static int getJTypeTagForPrimitive(String typeName) {

        return switch (typeName) {
            case JBYTE_KIND -> JBYTE;
            case JCHAR_KIND -> JCHAR;
            case JSHORT_KIND -> JSHORT;
            case JINT_KIND -> JINT;
            case JLONG_KIND -> JLONG;
            case JFLOAT_KIND -> JFLOAT;
            case JDOUBLE_KIND -> JDOUBLE;
            case JBOOLEAN_KIND -> JBOOLEAN;
            default -> throw new IllegalArgumentException("The Java " + typeName + " type is not yet supported.");
        };
    }

    public static JArrayType getJArrayTypeFromTypeName(String typeName, byte dimensions) {

        JArrayType arrayType = new JArrayType(getJTypeFromTypeName(typeName));
        int i = 1;
        while (i < ((int) dimensions)) {
            arrayType = new JArrayType(arrayType);
            i += 1;
        }

        return arrayType;
    }

    public static JType getJTypeForBType(BType type) {
        return switch (type.tag) {
            case INT -> jLong;
            case BYTE -> jInt;
            case BOOLEAN -> jBoolean;
            case FLOAT -> jFloat;
            default -> new JRefType(OBJECT);
        };
    }

    /**
     * Java array type.
     *
     * @since 1.2.0
     */
    public static class JArrayType extends JType {

        public JType elementType;

        JArrayType(JType elementType) {

            super(JARRAY);
            this.elementType = elementType;
        }
    }

    /**
     * Java referenced type.
     *
     * @since 1.2.0
     */
    public static class JRefType extends JType {

        public String typeValue;
        public boolean isInterface = false;
        public boolean isArray = false;

        public JRefType(String typeValue) {

            super(JREF);
            this.typeValue = typeValue;
        }
    }
}
