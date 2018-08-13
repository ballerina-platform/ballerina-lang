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
package org.ballerinalang.persistence.serializable.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Helper methods to operate on Objects.
 */
public class ObjectHelper {
    private ObjectHelper() {}

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
            if (Modifier.isTransient(declaredField.getModifiers())) {
                // transient fields should not be serialized
                continue;
            }
            String name = declaredField.getName();
            if (depth > 0) {
                name = name + "#" + depth;
            }
            fieldMap.put(name, declaredField);
        }
        if (targetClass != Object.class) {
            fieldMap.putAll(getAllFields(targetClass.getSuperclass(), depth + 1));
        }
        return fieldMap;
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

        if (targetType == byte.class) {
            return getByte((Long) obj);
        }

        if (targetType == Byte.class) {
            return new Byte(getByte((Long) obj));
        }

        if (targetType == char.class) {
            return getChar((Long) obj);
        }

        if (targetType == Character.class) {
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

}
