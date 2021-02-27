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
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Helper methods to operate on Objects.
 *
 * @since 0.982.0
 */
class ObjectHelper {
    private static final String BVALUE_PACKAGE_PATH = getBValuePackagePath();
    public static final String DEPTH_SEP = "#";

    private ObjectHelper() {
    }

    /**
     * Get all fields in the class hierarchy until Object class.
     * <p>
     * Skip {@code transient} and compile time constants.
     *
     * @param targetClass from which the fields are inspected.
     * @param depth       depth in the hierarchy; starting class should be 0
     * @return map of Field objects, field names encoded with class hierarchy depth.
     */
    static HashMap<String, Field> getAllFields(Class<?> targetClass, int depth) {
        HashMap<String, Field> fieldMap = new HashMap<>();
        for (Field declaredField : targetClass.getDeclaredFields()) {
            if (skip(declaredField)) {
                continue;
            }
            String name;
            if (depth == 0) {
                name = declaredField.getName();
            } else {
                name = declaredField.getName() + DEPTH_SEP + depth;
            }
            fieldMap.put(name, declaredField);
        }
        if (targetClass != Object.class) {
            fieldMap.putAll(getAllFields(targetClass.getSuperclass(), depth + 1));
        }
        return fieldMap;
    }

    /**
     * Should this field be skipped from serialization process.
     * <p>
     * This is mostly because this field is a transient field or this is a compile time constant field.
     *
     * @param declaredField Field object under consideration.
     * @return whether this field should be skipped or not.
     */
    private static boolean skip(Field declaredField) {
        int modifiers = declaredField.getModifiers();
        if (Modifier.isTransient(modifiers)) {
            // transient fields should not be serialized
            return true;
        }
        return compileTimeConst(modifiers, declaredField.getType());
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
     * Set {@code obj} into {@code field} of {@code target} object.
     * <p>
     * This is similar to {@code target.field = obj}.
     *
     * @param target Set the field on this object.
     * @param field  Field instance to be set.
     * @param obj    Set this object to the field.
     */
    static void setField(Object target, Field field, Object obj) {
        primeFinalFieldForAssignment(field);
        try {
            field.set(target, obj);
        } catch (IllegalAccessException e) {
            // Ignore it, this is fine.
            // Reason: Either security manager stopping us from setting this field
            // or this is a static final field initialized using compile time constant,
            // we can't assign to them at runtime, nor can we identify them at runtime.
            String targetClassName = target.getClass().getName();
            String fieldName = field.getName();
            throw new BallerinaException(String.format(
                    "Can not set field '%s' of '%s' via reflective deserialization, please provide a %s for %s",
                    fieldName, targetClassName, SerializationBValueProvider.class.getSimpleName(), targetClassName),
                    e);
        } catch (IllegalArgumentException e) {
            throw new BallerinaException(e);
        }
    }

    private static void primeFinalFieldForAssignment(Field field) {
        try {
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BallerinaException(e);
        }
    }

    /**
     * If special method {@code readResolve} is available in this {@code clz} class, invoke it.
     * <p>
     * See java.io.Serializable documentation for more information about 'readResolve'.
     */
    static Object invokeReadResolveOn(Object object, Class<?> clz) {
        try {
            Method readResolved = clz.getDeclaredMethod("readResolve");
            if (readResolved != null) {
                readResolved.setAccessible(true);
                return readResolved.invoke(object);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // no op, will return null from end of this method.
        }
        return null;
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

    static boolean isInstantiable(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isInterface(modifiers) && Modifier.isAbstract(modifiers));
    }

    static Class<?> findPrimitiveClass(String typeName) {
        switch (typeName) {
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            default:
                return null;
        }
    }
}
