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
package org.ballerinalang.nativeimpl.java;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.StringValue;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.JAVA_CLASS_NOT_FOUND_ERROR;

/**
 * This class contains various utility functions required to provide the 'ballerina/java' module API.
 *
 * @since 1.0.0
 */
public class JavaUtils {
    private static final String booleanTypeName = "boolean";
    private static final String byteTypeName = "byte";
    private static final String shortTypeName = "short";
    private static final String charTypeName = "char";
    private static final String intTypeName = "int";
    private static final String longTypeName = "long";
    private static final String floatTypeName = "float";
    private static final String doubleTypeName = "double";


    /**
     * Returns the Java Class object associated with the class or interface with the given string name.
     *
     * @param name   class name
     * @return a Java Class object instance
     */
    public static Object getClass(StringValue name) {
        Class<?> clazz = getPrimitiveTypeClass(String.valueOf(name));
        if (clazz != null) {
            return new HandleValue(clazz);
        }

        try {
            clazz = Class.forName(String.valueOf(name));
            return new HandleValue(clazz);
        } catch (ClassNotFoundException e) {
            return BallerinaErrors.createError(JAVA_CLASS_NOT_FOUND_ERROR, String.valueOf(name));
        }
    }

    private static Class<?> getPrimitiveTypeClass(String name) {
        switch (name) {
            case booleanTypeName:
                return Boolean.TYPE;
            case byteTypeName:
                return Byte.TYPE;
            case shortTypeName:
                return Short.TYPE;
            case charTypeName:
                return Character.TYPE;
            case intTypeName:
                return Integer.TYPE;
            case longTypeName:
                return Long.TYPE;
            case floatTypeName:
                return Float.TYPE;
            case doubleTypeName:
                return Double.TYPE;
            default:
                return null;
        }
    }
}
