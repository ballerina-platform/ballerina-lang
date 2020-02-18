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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInteropException.CLASS_NOT_FOUND_REASON;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInteropException.UNSUPPORTED_PRIMITIVE_TYPE_REASON;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JARRAY_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JBOOLEAN_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JBYTE_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JCHAR_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JDOUBLE_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JFLOAT_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JINT_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JLONG_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JREF_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JSHORT_KIND;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.JVOID_KIND;

/**
 * This class contains a set of utility methods and constants used in this implementation.
 *
 * @since 1.0.0
 */
class JInterop {

    static final String INTEROP_ANNOT_ORG = "ballerinax";
    static final String INTEROP_ANNOT_MODULE = "java";

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
            switch (primitiveName) {
                case JBYTE_KIND:
                    return JType.jByte;
                case JCHAR_KIND:
                    return JType.jChar;
                case JSHORT_KIND:
                    return JType.jShort;
                case JINT_KIND:
                    return JType.jInt;
                case JLONG_KIND:
                    return JType.jLong;
                case JFLOAT_KIND:
                    return JType.jFloat;
                case JDOUBLE_KIND:
                    return JType.jDouble;
                case JBOOLEAN_KIND:
                    return JType.jBoolean;
                case JVOID_KIND:
                    return JType.jVoid;
                default:
                    throw new IllegalArgumentException("The Java " + primitiveName + " type is not yet supported.");
            }
        } else if (jTypeClass == Void.class) {
            throw new IllegalArgumentException("The Java Void type is not yet supported.");
        } else if (jTypeClass.isArray()) {
            return JType.getJArrayTypeFromTypeName(jTypeClass.getComponentType().getName(), (byte) 0);
        }

        JType.JRefType jRefType = new JType.JRefType(jTypeClass.getName().replace('.', '/'));
        jRefType.isArray = jTypeClass.isArray();
        jRefType.isInterface = jTypeClass.isInterface();

        return jRefType;
    }


    static String getMethodSig(Class<?> returnType, Class<?>... parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> type : parameterTypes) {
            sb.append(getSig(type));
        }
        sb.append(')');
        return sb.append(getSig(returnType)).toString();
    }

    static String getSig(Class<?> c) {
        if (c.isPrimitive()) {
            if (int.class == c) {
                return "I";
            } else if (long.class == c) {
                return "J";
            } else if (boolean.class == c) {
                return "Z";
            } else if (byte.class == c) {
                return "B";
            } else if (short.class == c) {
                return "S";
            } else if (char.class == c) {
                return "C";
            } else if (float.class == c) {
                return "F";
            } else if (double.class == c) {
                return "D";
            } else {
                // This is void
                return "V";
            }
        } else if (void.class == c || Void.class == c) {
            return "V";
        } else {
            String className = c.getName().replace('.', '/');
            if (c.isArray()) {
                return className;
            } else {
                return 'L' + className + ';';
            }
        }
    }

    static ParamTypeConstraint[] buildParamTypeConstraints(List<JType> javaTypeConstraints, ClassLoader classLoader) {
        if (javaTypeConstraints == null) {
            return new ParamTypeConstraint[0];
        }

        List<ParamTypeConstraint> constraintList = new ArrayList<>();
        for (JType javaTypeConstraint : javaTypeConstraints) {
            constraintList.add(buildParamTypeConstraint(javaTypeConstraint, classLoader));
        }
        return constraintList.toArray(new ParamTypeConstraint[0]);
    }
    private static ParamTypeConstraint buildParamTypeConstraint(JType javaTypeConstraint, ClassLoader classLoader) {
        switch (javaTypeConstraint.type) {
            case JREF_KIND:
                return buildConstraintFromJavaRefType((JType.JRefType) javaTypeConstraint, classLoader);
            case JType.JARRAY_KIND:
                return buildConstraintFromJavaArrayType((JType.JArrayType) javaTypeConstraint, classLoader);
            case JType.JNO_KIND:
                return ParamTypeConstraint.NO_CONSTRAINT;
            default:
                return buildConstraintFromJavaPrimitiveType(javaTypeConstraint);
        }
    }

    private static ParamTypeConstraint buildConstraintFromJavaRefType(JType.JRefType javaRefType, ClassLoader classLoader) {
        String constraintBValue = javaRefType.type;
        return new ParamTypeConstraint(loadClass(constraintBValue, classLoader));
    }

    private static ParamTypeConstraint buildConstraintFromJavaArrayType(JType.JArrayType jArrayType, ClassLoader classLoader) {
        String typeSig = getJavaArrayTypeSig(jArrayType);
        return new ParamTypeConstraint(loadClass(typeSig, classLoader));
    }

    private static String getJavaArrayTypeSig(JType.JArrayType jArrayType) {
        JType elementType = jArrayType.elementType;
        String elementTypeSig = "[";
        String tagValue = elementType.type;
        if (tagValue.equals(JREF_KIND)) {
            elementTypeSig += "L" + ((JType.JRefType) elementType).type + ";";
        } else if (tagValue.equals(JARRAY_KIND)) {
            elementTypeSig += getJavaArrayTypeSig((JType.JArrayType) elementType);
        } else {
            elementTypeSig += getSignatureFromJavaPrimitiveType(elementType);
        }

        return elementTypeSig;
    }

    private static ParamTypeConstraint buildConstraintFromJavaPrimitiveType(JType primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        Class<?> constraintClass;
        switch (primitiveTypeName.type) {
            case "byte":
                constraintClass = byte.class;
                break;
            case "short":
                constraintClass = short.class;
                break;
            case "char":
                constraintClass = char.class;
                break;
            case "int":
                constraintClass = int.class;
                break;
            case "long":
                constraintClass = long.class;
                break;
            case "float":
                constraintClass = float.class;
                break;
            case "double":
                constraintClass = double.class;
                break;
            case "boolean":
                constraintClass = boolean.class;
                break;
            default:
                throw new JInteropException(UNSUPPORTED_PRIMITIVE_TYPE_REASON,
                        "Unsupported Java primitive type '" + primitiveTypeName + "'");
        }
        return new ParamTypeConstraint(constraintClass);
    }

    private static String getSignatureFromJavaPrimitiveType(JType primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        switch (primitiveTypeName.type) {
            case "byte":
                return "B";
            case "short":
                return "S";
            case "char":
                return "C";
            case "int":
                return "I";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            case "boolean":
                return "Z";
            default:
                throw new JInteropException(UNSUPPORTED_PRIMITIVE_TYPE_REASON,
                        "Unsupported Java primitive type '" + primitiveTypeName + "'");
        }
    }

    static Class<?> loadClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className.replace("/", "."), false, classLoader);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND_REASON, e.getMessage(), e);
        }
    }

    static BLangCompilerException createJInteropError(String reason, String details) {
        return new BLangCompilerException("error " + reason +
                Optional.ofNullable(details).map(d -> " " + StringUtils.getStringValue(d)).orElse(""));
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
        switch (annotTag) {
            case CONSTRUCTOR_ANNOT_TAG:
            case METHOD_ANNOT_TAG:
            case FIELD_GET_ANNOT_TAG:
            case FIELD_PUT_ANNOT_TAG:
                return true;
            default:
                return false;
        }
    }

    static boolean isMethodAnnotationTag(String annotTag) {
        return CONSTRUCTOR_ANNOT_TAG.equals(annotTag) || METHOD_ANNOT_TAG.equals(annotTag);
    }
}
