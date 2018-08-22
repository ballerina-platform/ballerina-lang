/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.util.serializer;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Helper methods to operate on Objects.
 */
public class ObjectHelper {
    private static final String BVALUE_PACKAGE_PATH = getBValuePackagePath();

    private ObjectHelper() {
    }

    /**
     * Get all fields in the class hierarchy until Object class.
     *
     * @param targetClass from which the fields are inspected.
     * @param depth       depth in the hierarchy; starting class should be 0
     * @return
     */
    public static HashMap<String, Field> getAllFields(Class<?> targetClass, int depth) {
        HashMap<String, Field> fieldMap = new HashMap<>();
        for (Field declaredField : targetClass.getDeclaredFields()) {
            int modifiers = declaredField.getModifiers();
            if (Modifier.isTransient(modifiers)) {
                // transient fields should not be serialized
                continue;
            }
            if (compileTimeConst(modifiers, declaredField.getType())) {
                continue;
            }
            String name;
            if (depth == 0) {
                name = declaredField.getName();
            } else {
                name = declaredField.getName() + "#" + depth;
            }
            fieldMap.put(name, declaredField);
        }
        if (targetClass != Object.class) {
            fieldMap.putAll(getAllFields(targetClass.getSuperclass(), depth + 1));
        }
        return fieldMap;
    }

    private static boolean compileTimeConst(int modifiers, Class<?> type) {
        return Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) &&
                (type == int.class
                        || type == long.class
                        || type == short.class
                        || type == byte.class
                        || type == char.class
                        || type == float.class
                        || type == double.class
                        || type == String.class);
    }

    /**
     * Convert to desired type if special conditions are matched.
     *
     * @param obj
     * @param targetType
     * @return
     */
    static Object cast(Object obj, Class targetType) {
        if (obj == null) {
            // get default value for primitive type
            if (targetType == int.class
                    || targetType == long.class
                    || targetType == char.class
                    || targetType == short.class
                    || targetType == byte.class) {
                return 0;
            } else if (targetType == float.class) {
                return 0.0f;
            } else if (targetType == double.class) {
                return 0.0;
            }
            return null;
        }

        Class<?> objClass = obj.getClass();
        // JsonParser always treat integer numbers as longs, if target field is int then cast to int.
        if ((targetType == Integer.class || targetType == int.class) && objClass == Long.class) {
            return ((Long) obj).intValue();
        }

        // JsonParser always treat float numbers as doubles, if target field is float then cast to float.
        if ((targetType == Float.class || targetType == float.class) && objClass == Double.class) {
            return ((Double) obj).floatValue();
        }

        if (targetType == byte.class && obj instanceof Long) {
            return getByte((Long) obj);
        }

        if (targetType == Byte.class && obj instanceof Long) {
            return new Byte(getByte((Long) obj));
        }

        if (targetType == char.class && obj instanceof Long) {
            return getChar((Long) obj);
        }

        if (targetType == Character.class && obj instanceof Long) {
            return new Character(getChar((Long) obj));
        }

        return obj;
    }

    private static byte getByte(Long obj) {
        return obj.byteValue();
    }

    private static char getChar(Long obj) {
        return (char) obj.byteValue();
    }

    static String getTrimmedClassName(Object obj) {
        Class<?> clazz = obj.getClass();
        return getTrimmedClassName(clazz);
    }

    static String getTrimmedClassName(Class<?> clazz) {
        String className = clazz.getName();
        // if obj is a BValue, trim fully qualified name to class name.
        if (BValue.class.isAssignableFrom(clazz) && className.startsWith(BVALUE_PACKAGE_PATH)) {
            className = className.substring(BVALUE_PACKAGE_PATH.length() + 1);
        }
        return className;
    }


    private static String getBValuePackagePath() {
        return BValue.class.getPackage().getName();
    }

    static Class<?> findComponentType(BRefValueArray valueArray, Class<?> targetType) {
        // if target type is a array then find its component type.
        // else (i.e. target type is Object) infer component type from JSON representation.
        if (valueArray.size() > 0 && !targetType.isArray()) {
            if (valueArray.get(0).getType().equals(org.ballerinalang.model.types.BTypes.typeString)) {
                return String.class;
            } else if (valueArray.get(0).getType().equals(org.ballerinalang.model.types.BTypes.typeInt)) {
                return Long.class;
            } else if (valueArray.get(0).getType().equals(BTypes.typeFloat)) {
                return Double.class;
            }
        }
        return targetType.getComponentType();
    }

    static boolean isInstantiable(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isInterface(modifiers) && Modifier.isAbstract(modifiers));
    }
}
