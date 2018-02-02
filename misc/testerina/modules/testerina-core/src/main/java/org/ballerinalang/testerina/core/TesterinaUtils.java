/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.testerina.core;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import static java.security.AccessController.doPrivileged;

/**
 * Utility functions for Function Invocations.
 *
 * @since 0.8.0
 */
public class TesterinaUtils {

    public static final String BALLERINA_MOCK_SYSPROPERTY = "ballerina.mock";

    private TesterinaUtils() {
    }

    public static void setMockEnabled(Boolean mock) {
        System.setProperty(BALLERINA_MOCK_SYSPROPERTY, mock.toString());
    }

    public static boolean isMockEnabled() {
        return Boolean.parseBoolean(System.getProperty(BALLERINA_MOCK_SYSPROPERTY));
    }

    /**
     * Utility method to get a Java field via reflection.
     */
    public static <T> T getField(Object instance, String fieldName, Class<T> fieldType) throws NoSuchFieldException {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            doPrivileged((PrivilegedAction<Object>) () -> {
                field.setAccessible(true);
                return null;
            });

            if (fieldType.isAssignableFrom(field.getType())) {
                return fieldType.cast(field.get(instance));
            }
        } catch (IllegalAccessException e) {
            //ignore - not thrown since #isAssignableFrom is checked
        }
        throw new BallerinaException("Error while modifying native connector with the mock value.");
    }

    /**
     * Utility method to invoke a Java method and get the result via reflection.
     */
    public static <T, V> V invokeMethod(T object, String methodName, Class<V> returnType)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = object.getClass().getMethod(methodName);
        doPrivileged((PrivilegedAction<Object>) () -> {
            method.setAccessible(true);
            return null;
        });

        return returnType.cast(method.invoke(object));
    }

    /**
     * Set the provided property value to the give object's fieldName.
     */
    public static <T> void setProperty(T instance, String fieldName, String value)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });

        if (field.getType() == Character.TYPE) {
            field.set(instance, value.charAt(0));
            return;
        }
        if (field.getType() == Short.TYPE) {
            field.set(instance, Short.parseShort(value));
            return;
        }
        if (field.getType() == Integer.TYPE) {
            field.set(instance, Integer.parseInt(value));
            return;
        }
        if (field.getType() == Long.TYPE) {
            field.set(instance, Long.parseLong(value));
            return;
        }
        if (field.getType() == Double.TYPE) {
            field.set(instance, Double.parseDouble(value));
            return;
        }
        if (field.getType() == Float.TYPE) {
            field.set(instance, Float.parseFloat(value));
            return;
        }
        if (field.getType() == Byte.TYPE) {
            field.set(instance, Byte.parseByte(value));
            return;
        }
        if (field.getType() == Boolean.TYPE) {
            field.set(instance, Boolean.parseBoolean(value));
            return;
        }
        field.set(instance, value);
    }

}
