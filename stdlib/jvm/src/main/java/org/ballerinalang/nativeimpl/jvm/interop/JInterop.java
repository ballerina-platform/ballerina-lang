/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.interop;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.nativeimpl.jvm.interop.JInteropException.CLASS_NOT_FOUND_REASON;
import static org.ballerinalang.nativeimpl.jvm.interop.JInteropException.UNSUPPORTED_PRIMITIVE_TYPE_REASON;

/**
 * This class contains a set of utility methods and constants used in this implementation.
 *
 * @since 1.0.0
 */
class JInterop {

    static final String ORG_NAME = "ballerina";
    static final String BALLERINA_X_ORG_NAME = "ballerinax";
    static final String MODULE_NAME = "jvm";
    static final String JAVA_MODULE_NAME = "java";
    static final String JVM_PACKAGE_PATH = ORG_NAME + ORG_NAME_SEPARATOR + MODULE_NAME;
    static final BPackage JVM_PACKAGE_ID = new BPackage(ORG_NAME, MODULE_NAME, "0.0.0");
    static final String ERROR_REASON_PREFIX = "{" + BALLERINA_X_ORG_NAME + "/" + JAVA_MODULE_NAME + "}";
    static final String PARAM_TYPE_CONSTRAINTS_FIELD = "paramTypeConstraints";
    static final String CLASS_FIELD = "class";
    static final String NAME_FIELD = "name";
    static final String KIND_FIELD = "kind";
    static final String IS_INTERFACE_FIELD = "isInterface";
    static final String IS_ARRAY_FIELD = "isArray";
    static final String IS_STATIC_FIELD = "isStatic";
    static final String REST_PARAM_EXIST_FIELD = "restParamExist";
    static final String SIG_FIELD = "sig";
    static final String METHOD_TYPE_FIELD = "mType";
    static final String FIELD_TYPE_FIELD = "fType";
    static final String PARAM_TYPES_FIELD = "paramTypes";
    static final String REST_TYPE_FIELD = "restType";
    static final String RETURN_TYPE_FIELD = "retType";
    static final String METHOD_FIELD = "method";
    static final String TAG_FIELD = "tag";
    static final String VALUES_FIELD = "values";
    static final String VALUE_FIELD = "value";

    static final String HANDLE_TYPE_NAME = "handle";
    static final String METHOD_TYPE_NAME = "Method";
    static final String FIELD_TYPE_NAME = "Field";
    static final String METHOD_TYPE_TYPE_NAME = "Method";
    static final String TYPE_NAME_FIELD = "typeName";
    static final String TYPE_VALUE_FIELD = "typeValue";
    static final String NO_TYPE_NAME = "NoType";
    static final String METHOD_THROWS_FIELD = "throws";
    static final String B_FUNC_TYPE_FIELD = "bFuncType";
    static final String ELEMENT_TYPE_FIELD = "elementType";
    static final String DIMENSIONS_FIELD = "dimensions";
    static final String UNION_TYPE_MEMBERS_FIELD = "members";
    static final String TUPLE_TYPE_MEMBERS_FIELD = "tupleTypes";
    static final String ARRAY_ELEMENT_TYPE_FIELD = "eType";
    static final String CLASS_LOADER_DATA = "class_loader";

    static final String RECORD_TNAME = "record";
    static final String OBJECT_TNAME = "object";
    static final String UNION_TNAME = "union";
    static final String TUPLE_TNAME = "tuple";
    static final String ARRAY_TNAME = "array";

    static final String PLATFORM_TYPE_NAME = "platform";
    static final String J_TYPE_KIND = "jTypeKind";

    static final String J_BYTE_TYPE_NAME = "JByte";
    static final String J_BYTE = "byte";
    static final String J_CHAR_TYPE_NAME = "JChar";
    static final String J_CHAR = "char";
    static final String J_SHORT_TYPE_NAME = "JShort";
    static final String J_SHORT = "short";
    static final String J_INT_TYPE_NAME = "JInt";
    static final String J_INT = "int";
    static final String J_LONG_TYPE_NAME = "JLong";
    static final String J_LONG = "long";
    static final String J_FLOAT_TYPE_NAME = "JFloat";
    static final String J_FLOAT = "float";
    static final String J_DOUBLE_TYPE_NAME = "JDouble";
    static final String J_DOUBLE = "double";
    static final String J_BOOLEAN_TYPE_NAME = "JBoolean";
    static final String J_BOOLEAN = "boolean";
    static final String J_VOID_TYPE_NAME = "JVoid";
    static final String J_VOID = "void";
    static final String J_REF_TYPE_NAME = "JRefType";
    static final String J_REF = "ref";
    static final String J_ARRAY_TYPE_NAME = "JArrayType";
    static final String J_ARRAY = "array";
    static final String J_NO_TYPE_NAME = "JNo";
    static final String J_NO = "no";

    //jvm type names
    static final String J_OBJECT_TNAME = Object.class.getTypeName();
    static final String J_STRING_TNAME = String.class.getTypeName();
    static final String J_BOOLEAN_OBJ_TNAME = Boolean.class.getTypeName();
    static final String J_INTEGER_OBJ_TNAME = Integer.class.getTypeName();
    static final String J_BYTE_OBJ_TNAME = Byte.class.getTypeName();
    static final String J_LONG_OBJ_TNAME = Long.class.getTypeName();
    static final String J_DOUBLE_OBJ_TNAME = Double.class.getTypeName();
    static final String J_FLOAT_OBJ_TNAME = Float.class.getTypeName();
    static final String J_PRIMITIVE_INT_TNAME = int.class.getTypeName();
    static final String J_PRIMITIVE_LONG_TNAME = long.class.getTypeName();
    static final String J_PRIMITIVE_BYTE_TNAME = byte.class.getTypeName();
    static final String J_PRIMITIVE_SHORT_TNAME = short.class.getTypeName();
    static final String J_PRIMITIVE_CHAR_TNAME = char.class.getTypeName();
    static final String J_PRIMITIVE_FLOAT_TNAME = float.class.getTypeName();
    static final String J_PRIMITIVE_DOUBLE_TNAME = double.class.getTypeName();
    static final String J_PRIMITIVE_BOOLEAN_TNAME = boolean.class.getTypeName();
    static final String J_VOID_TNAME = void.class.getTypeName();
    private static final BArrayType anydataArrayType = new BArrayType(BTypes.typeAnydata);


    static MapValue<String, Object> createRecordBValue(String typeName) {
        return BallerinaValues.createRecordValue(JVM_PACKAGE_ID, typeName);
    }

    static MapValue<String, Object> createJMethodTypeBValue(JMethod jMethod) {
        MapValue<String, Object> jMethodTypeBRecord = createRecordBValue(METHOD_TYPE_TYPE_NAME);

        ArrayValue paramBTypeArray = (ArrayValue) BValueCreator.createArrayValue(anydataArrayType);
        Class<?>[] paramClassTypes = jMethod.getParamTypes();
        for (int paramIndex = 0; paramIndex < paramClassTypes.length; paramIndex++) {
            Class<?> paramClassType = paramClassTypes[paramIndex];
            Object jParamType = createJTypeBValue(paramClassType);
            paramBTypeArray.add(paramIndex, jParamType);
        }
        jMethodTypeBRecord.put(PARAM_TYPES_FIELD, paramBTypeArray);

        Class<?> retClassType = jMethod.getReturnType();
        jMethodTypeBRecord.put(RETURN_TYPE_FIELD, createJTypeBValue(retClassType));
        return jMethodTypeBRecord;
    }

    static Object createJTypeBValue(Class<?> jTypeClass) {
        if (jTypeClass.isPrimitive()) {
            String primitiveName = jTypeClass.getName();
            MapValue<String, Object> jPrimitiveTypeBRecord;
            switch (primitiveName) {
                case J_BYTE:
                    jPrimitiveTypeBRecord = createRecordBValue(J_BYTE_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_BYTE);
                    break;
                case J_CHAR:
                    jPrimitiveTypeBRecord = createRecordBValue(J_CHAR_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_CHAR);
                    break;
                case J_SHORT:
                    jPrimitiveTypeBRecord = createRecordBValue(J_SHORT_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_SHORT);
                    break;
                case J_INT:
                    jPrimitiveTypeBRecord = createRecordBValue(J_INT_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_INT);
                    break;
                case J_LONG:
                    jPrimitiveTypeBRecord = createRecordBValue(J_LONG_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_LONG);
                    break;
                case J_FLOAT:
                    jPrimitiveTypeBRecord = createRecordBValue(J_FLOAT_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_FLOAT);
                    break;
                case J_DOUBLE:
                    jPrimitiveTypeBRecord = createRecordBValue(J_DOUBLE_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_DOUBLE);
                    break;
                case J_BOOLEAN:
                    jPrimitiveTypeBRecord = createRecordBValue(J_BOOLEAN_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_BOOLEAN);
                    break;
                case J_VOID:
                    jPrimitiveTypeBRecord = createRecordBValue(J_VOID_TYPE_NAME);
                    jPrimitiveTypeBRecord.put(J_TYPE_KIND, J_VOID);
                    break;
                default:
                    throw new IllegalArgumentException("The Java " + primitiveName + " type is not yet supported.");
            }
            return jPrimitiveTypeBRecord;
        } else if (jTypeClass == Void.class) {
            throw new IllegalArgumentException("The Java Void type is not yet supported.");
        } else if (jTypeClass.isArray()) {
            MapValue<String, Object> jArrayTypeBRecord = createRecordBValue(J_ARRAY_TYPE_NAME);
            jArrayTypeBRecord.put(ELEMENT_TYPE_FIELD, createJTypeBValue(jTypeClass.getComponentType()));
            return jArrayTypeBRecord;
        }

        MapValue<String, Object> jRefTypeBRecord = createRecordBValue(J_REF_TYPE_NAME);
        jRefTypeBRecord.put(TAG_FIELD, J_REF_TYPE_NAME);
        jRefTypeBRecord.put(TYPE_VALUE_FIELD, jTypeClass.getName().replace('.', '/'));
        jRefTypeBRecord.put(IS_INTERFACE_FIELD, jTypeClass.isInterface());
        jRefTypeBRecord.put(IS_ARRAY_FIELD, jTypeClass.isArray());
        return jRefTypeBRecord;
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

    static boolean isHandleType(Object bValue) {
        if (bValue instanceof MapValue) {
            MapValue<String, Object> bTypeValue = (MapValue<String, Object>) bValue;
            String handleTypeName = (String) bTypeValue.get(TYPE_VALUE_FIELD);
            return handleTypeName.equals(HANDLE_TYPE_NAME);
        }
        return false;
    }

    static ParamTypeConstraint[] buildParamTypeConstraints(ArrayValue javaTypeConstraints, ClassLoader classLoader) {
        if (javaTypeConstraints == null) {
            return new ParamTypeConstraint[0];
        }

        List<ParamTypeConstraint> constraintList = new ArrayList<>();
        for (int paramIndex = 0; paramIndex < javaTypeConstraints.size(); paramIndex++) {
            Object javaTypeConstraint = javaTypeConstraints.get(paramIndex);
            constraintList.add(buildParamTypeConstraint(javaTypeConstraint, classLoader));
        }
        return constraintList.toArray(new ParamTypeConstraint[0]);
    }

    private static ParamTypeConstraint buildParamTypeConstraint(Object javaTypeConstraint, ClassLoader classLoader) {
        if (isJavaRefType(javaTypeConstraint)) {
            return buildConstraintFromJavaRefType((MapValue<String, Object>) javaTypeConstraint, classLoader);
        } else if (isJavaArrayType(javaTypeConstraint)) {
            return buildConstraintFromJavaArrayType((MapValue<String, Object>) javaTypeConstraint, classLoader);
        } else if (isJavaNoType(javaTypeConstraint)) {
            return ParamTypeConstraint.NO_CONSTRAINT;
        } else {
            return buildConstraintFromJavaPrimitiveType((MapValue<String, Object>) javaTypeConstraint);
        }
    }

    private static ParamTypeConstraint buildConstraintFromJavaRefType(MapValue<String, Object> javaRefType,
                ClassLoader classLoader) {
        String constraintBValue = (String) javaRefType.get(TYPE_VALUE_FIELD);
        return new ParamTypeConstraint(loadClass(constraintBValue, classLoader));
    }

    private static ParamTypeConstraint buildConstraintFromJavaArrayType(MapValue<String, Object> javaRefType,
                                                                        ClassLoader classLoader) {
        String typeSig = getJavaArrayTypeSig(javaRefType);
        return new ParamTypeConstraint(loadClass(typeSig, classLoader));
    }

    private static String getJavaArrayTypeSig(MapValue<String, Object> javaRefType) {
        Object elementType = javaRefType.get(ELEMENT_TYPE_FIELD);
        String elementTypeSig = "[";
        MapValue<String, Object> jRefTypeBValue = (MapValue<String, Object>) elementType;
        String tagValue = (String) jRefTypeBValue.get(J_TYPE_KIND);
        if (tagValue.equals(J_REF)) {
            elementTypeSig += "L" + (String) jRefTypeBValue.get(TYPE_VALUE_FIELD) + ";";
        } else if (tagValue.equals(J_ARRAY)) {
            elementTypeSig += getJavaArrayTypeSig(jRefTypeBValue);
        } else {
            elementTypeSig += getSignatureFromJavaPrimitiveType((MapValue<String, Object>) elementType);
        }

        return elementTypeSig;
    }

    private static ParamTypeConstraint buildConstraintFromJavaPrimitiveType(MapValue<String, Object>
                                                                                    primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        Class<?> constraintClass;
        switch (primitiveTypeName.get(J_TYPE_KIND).toString()) {
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

    private static String getSignatureFromJavaPrimitiveType(MapValue<String, Object> primitiveTypeName) {
        // Java primitive types: byte, short, char, int, long, float, double, boolean
        switch (primitiveTypeName.get(J_TYPE_KIND).toString()) {
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

    private static boolean isJavaRefType(Object javaTypeConstraint) {
        MapValue jRefTypeBValue = (MapValue) javaTypeConstraint;
        String tagValue = (String) jRefTypeBValue.get(J_TYPE_KIND);
        return tagValue != null && tagValue.equals(J_REF);
    }

    private static boolean isJavaArrayType(Object javaTypeConstraint) {
        MapValue jRefTypeBValue = (MapValue) javaTypeConstraint;
        String tagValue = (String) jRefTypeBValue.get(J_TYPE_KIND);
        return tagValue != null && tagValue.equals(J_ARRAY);
    }

    private static boolean isJavaNoType(Object javaTypeConstraint) {
        MapValue jRefTypeBValue = (MapValue) javaTypeConstraint;
        String tagValue = (String) jRefTypeBValue.get(J_TYPE_KIND);
        return tagValue != null && tagValue.equals(J_NO);
    }

    static Class<?> loadClass(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className.replace("/", "."), false, classLoader);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND_REASON, e.getMessage(), e);
        }
    }

    static ErrorValue createErrorBValue(String reason, String details) {
        MapValue<String, Object> refData = new MapValueImpl<>(BTypes.typeError.detailType);
        if (details != null) {
            refData.put("message", details);
        } else {
            refData.put("message", "");
        }
        return new ErrorValue(BTypes.typeError, ERROR_REASON_PREFIX + reason, refData);
    }
}
