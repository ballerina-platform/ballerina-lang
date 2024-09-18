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

import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.bir.codegen.exceptions.JInteropException;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JMethodKind;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JType;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.codegen.model.JType.getJTypeForPrimitive;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JBYTE;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JCHAR;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JDOUBLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JFLOAT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JINT;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JLONG;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JNO;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JREF;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags.JSHORT;

/**
 * This class contains a set of utility methods and constants used in this implementation.
 *
 * @since 1.2.0
 */
final class JInterop {

    static final String INTEROP_ANNOT_ORG = "ballerina";
    static final String INTEROP_ANNOT_MODULE = "jballerina.java";

    static final String CONSTRUCTOR_ANNOT_TAG = "Constructor";
    static final String METHOD_ANNOT_TAG = "Method";
    static final String FIELD_GET_ANNOT_TAG = "FieldGet";
    static final String FIELD_PUT_ANNOT_TAG = "FieldSet";

    //jvm type names
    static final String J_OBJECT_TNAME = Object.class.getTypeName();
    static final String J_STRING_TNAME = String.class.getTypeName();
    static final String J_BOOLEAN_OBJ_TNAME = Boolean.class.getTypeName();
    static final String J_INTEGER_OBJ_TNAME = Integer.class.getTypeName();
    static final String J_LONG_OBJ_TNAME = Long.class.getTypeName();
    static final String J_DOUBLE_OBJ_TNAME = Double.class.getTypeName();
    static final String J_PRIMITIVE_INT_TNAME = int.class.getTypeName();
    static final String J_PRIMITIVE_LONG_TNAME = long.class.getTypeName();
    static final String J_PRIMITIVE_BYTE_TNAME = byte.class.getTypeName();
    static final String J_PRIMITIVE_SHORT_TNAME = short.class.getTypeName();
    static final String J_PRIMITIVE_CHAR_TNAME = char.class.getTypeName();
    static final String J_PRIMITIVE_FLOAT_TNAME = float.class.getTypeName();
    static final String J_PRIMITIVE_DOUBLE_TNAME = double.class.getTypeName();
    static final String J_PRIMITIVE_BOOLEAN_TNAME = boolean.class.getTypeName();
    static final String J_VOID_TNAME = void.class.getTypeName();

    static JType getJType(Class<?> jTypeClass) {

        if (jTypeClass.isPrimitive()) {
            String primitiveName = jTypeClass.getName();
            return getJTypeForPrimitive(primitiveName);
        } else if (jTypeClass == Void.class) {
            throw new IllegalArgumentException("The Java Void type is not yet supported.");
        } else if (jTypeClass.isArray()) {
            return JType.getJArrayTypeFromTypeName(jTypeClass.getComponentType().getName(), (byte) 0);
        }

        JType.JRefType jRefType = new JType.JRefType(jTypeClass.getName().replace('.', '/'));
        jRefType.isArray = false;
        jRefType.isInterface = jTypeClass.isInterface();

        return jRefType;
    }

    static ParamTypeConstraint[] buildParamTypeConstraints(List<JType> javaTypeConstraints, ClassLoader classLoader) {

        if (javaTypeConstraints == null) {
            // Returning null because empty array signifies that the parameterTypes field is set but is empty
            // Null signifies that parameterTypes field has not been set
            return null;
        }

        List<ParamTypeConstraint> constraintList = new ArrayList<>();
        for (JType javaTypeConstraint : javaTypeConstraints) {
            constraintList.add(buildParamTypeConstraint(javaTypeConstraint, classLoader));
        }
        return constraintList.toArray(new ParamTypeConstraint[0]);
    }

    private static ParamTypeConstraint buildParamTypeConstraint(JType javaTypeConstraint, ClassLoader classLoader) {

        return switch (javaTypeConstraint.jTag) {
            case JREF -> buildConstraintFromJavaRefType((JType.JRefType) javaTypeConstraint, classLoader);
            case JARRAY -> buildConstraintFromJavaArrayType((JType.JArrayType) javaTypeConstraint, classLoader);
            case JNO -> ParamTypeConstraint.NO_CONSTRAINT;
            default -> buildConstraintFromJavaPrimitiveType(javaTypeConstraint);
        };
    }

    private static ParamTypeConstraint buildConstraintFromJavaRefType(JType.JRefType javaRefType,
                                                                      ClassLoader classLoader) {

        String constraintBValue = javaRefType.typeValue;
        return new ParamTypeConstraint(loadClass(constraintBValue, classLoader));
    }

    private static ParamTypeConstraint buildConstraintFromJavaArrayType(JType.JArrayType jArrayType,
                                                                        ClassLoader classLoader) {

        String typeSig = getJavaArrayTypeSig(jArrayType);
        return new ParamTypeConstraint(loadClass(typeSig, classLoader));
    }

    private static String getJavaArrayTypeSig(JType.JArrayType jArrayType) {

        JType elementType = jArrayType.elementType;
        String elementTypeSig = "[";
        int jTag = elementType.jTag;
        if (jTag == JREF) {
            elementTypeSig += "L" + ((JType.JRefType) elementType).typeValue + ";";
        } else if (jTag == JARRAY) {
            elementTypeSig += getJavaArrayTypeSig((JType.JArrayType) elementType);
        } else {
            elementTypeSig += getSignatureFromJavaPrimitiveType(elementType);
        }

        return elementTypeSig;
    }

    private static ParamTypeConstraint buildConstraintFromJavaPrimitiveType(JType primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        Class<?> constraintClass = switch (primitiveTypeName.jTag) {
            case JBYTE -> byte.class;
            case JSHORT -> short.class;
            case JCHAR -> char.class;
            case JINT -> int.class;
            case JLONG -> long.class;
            case JFLOAT -> float.class;
            case JDOUBLE -> double.class;
            case JBOOLEAN -> boolean.class;
            default -> throw new JInteropException(DiagnosticErrorCode.UNSUPPORTED_PRIMITIVE_TYPE,
                    "Unsupported Java primitive type '" + primitiveTypeName + "'");
        };
        return new ParamTypeConstraint(constraintClass);
    }

    private static String getSignatureFromJavaPrimitiveType(JType primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        return switch (primitiveTypeName.jTag) {
            case JBYTE -> "B";
            case JSHORT -> "S";
            case JCHAR -> "C";
            case JINT -> "I";
            case JLONG -> "J";
            case JFLOAT -> "F";
            case JDOUBLE -> "D";
            case JBOOLEAN -> "Z";
            default -> throw new JInteropException(DiagnosticErrorCode.UNSUPPORTED_PRIMITIVE_TYPE,
                    "Unsupported Java primitive type '" + primitiveTypeName + "'");
        };
    }

    static Class<?> loadClass(String className, ClassLoader classLoader) {

        try {
            return Class.forName(className.replace("/", "."), false, classLoader);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(DiagnosticErrorCode.CLASS_NOT_FOUND, e.getMessage(), e);
        }
    }

    static JMethodKind getMethodKindFromAnnotTag(String annotTagRef) {

        if (CONSTRUCTOR_ANNOT_TAG.equals(annotTagRef)) {
            return JMethodKind.CONSTRUCTOR;
        } else {
            return JMethodKind.METHOD;
        }
    }

    static JFieldMethod getFieldMethodFromAnnotTag(String annotTagRef) {

        if (FIELD_GET_ANNOT_TAG.equals(annotTagRef)) {
            return JFieldMethod.ACCESS;
        } else {
            return JFieldMethod.MUTATE;
        }
    }

    static boolean isInteropAnnotationTag(String annotTag) {

        return switch (annotTag) {
            case CONSTRUCTOR_ANNOT_TAG, METHOD_ANNOT_TAG, FIELD_GET_ANNOT_TAG, FIELD_PUT_ANNOT_TAG -> true;
            default -> false;
        };
    }

    static boolean isMethodAnnotationTag(String annotTag) {

        return CONSTRUCTOR_ANNOT_TAG.equals(annotTag) || METHOD_ANNOT_TAG.equals(annotTag);
    }

    private JInterop() {
    }
}
