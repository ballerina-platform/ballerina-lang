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

import io.ballerina.runtime.api.TypeTags;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JBYTE;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JCHAR;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JDOUBLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JFLOAT;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JINT;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JLONG;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JREF;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JSHORT;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags.JVOID;

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
    static JType jVoid = new JType(JVOID);
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
            case JVOID_KIND:
                return jVoid;
            default:
                return new JRefType(typeName.replace('.', '/'));
        }
    }

    static JType getJTypeForPrimitive(String typeName) {

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
            case JVOID_KIND:
                return jVoid;
            default:
                throw new IllegalArgumentException("The Java " + typeName + " type is not yet supported.");
        }
    }

    static int getJTypeTagForPrimitive(String typeName) {

        switch (typeName) {
            case JBYTE_KIND:
                return JBYTE;
            case JCHAR_KIND:
                return JCHAR;
            case JSHORT_KIND:
                return JSHORT;
            case JINT_KIND:
                return JINT;
            case JLONG_KIND:
                return JLONG;
            case JFLOAT_KIND:
                return JFLOAT;
            case JDOUBLE_KIND:
                return JDOUBLE;
            case JBOOLEAN_KIND:
                return JBOOLEAN;
            default:
                throw new IllegalArgumentException("The Java " + typeName + " type is not yet supported.");
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

    static JType getPrimitiveJTypeForBType(BType type) {
        switch (type.tag) {
            case TypeTags.INT_TAG:
                return jLong;
            case TypeTags.BYTE_TAG:
                return jInt;
            case TypeTags.BOOLEAN_TAG:
                return jBoolean;
            case TypeTags.FLOAT_TAG:
                return jFloat;
            default:
                return new JType(JREF);
        }
    }

    /**
     * Java array type.
     *
     * @since 1.2.0
     */
    public static class JArrayType extends JType {

        JType elementType;

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
        boolean isInterface = false;
        boolean isArray = false;

        public JRefType(String typeValue) {

            super(JREF);
            this.typeValue = typeValue;
        }
    }
}
