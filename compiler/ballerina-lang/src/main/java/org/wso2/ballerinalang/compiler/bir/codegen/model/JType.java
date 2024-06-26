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
    public static final JType J_VOID = new JType(JVOID);
    private static final JType J_BYTE = new JType(JBYTE);
    private static final JType J_CHAR = new JType(JCHAR);
    private static final JType J_SHORT = new JType(JSHORT);
    private static final JType J_INT = new JType(JINT);
    private static final JType J_LONG = new JType(JLONG);
    private static final JType J_FLOAT = new JType(JFLOAT);
    private static final JType J_DOUBLE = new JType(JDOUBLE);
    private static final JType J_BOOLEAN = new JType(JBOOLEAN);
    public int jTag;

    JType(int jTag) {

        super(JTypeTags.JTYPE, null);
        this.jTag = jTag;
    }

    public static JType getJTypeFromTypeName(String typeName) {

        return switch (typeName) {
            case JBYTE_KIND -> J_BYTE;
            case JCHAR_KIND -> J_CHAR;
            case JSHORT_KIND -> J_SHORT;
            case JINT_KIND -> J_INT;
            case JLONG_KIND -> J_LONG;
            case JFLOAT_KIND -> J_FLOAT;
            case JDOUBLE_KIND -> J_DOUBLE;
            case JBOOLEAN_KIND -> J_BOOLEAN;
            case JVOID_KIND -> J_VOID;
            default -> new JRefType(typeName.replace('.', '/'));
        };
    }

    public static JType getJTypeForPrimitive(String typeName) {

        return switch (typeName) {
            case JBYTE_KIND -> J_BYTE;
            case JCHAR_KIND -> J_CHAR;
            case JSHORT_KIND -> J_SHORT;
            case JINT_KIND -> J_INT;
            case JLONG_KIND -> J_LONG;
            case JFLOAT_KIND -> J_FLOAT;
            case JDOUBLE_KIND -> J_DOUBLE;
            case JBOOLEAN_KIND -> J_BOOLEAN;
            case JVOID_KIND -> J_VOID;
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
            case INT -> J_LONG;
            case BYTE -> J_INT;
            case BOOLEAN -> J_BOOLEAN;
            case FLOAT -> J_FLOAT;
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

        public JArrayType(JType elementType) {
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
