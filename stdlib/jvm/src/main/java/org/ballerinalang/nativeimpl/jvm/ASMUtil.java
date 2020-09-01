/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.jvm;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * A util class to handle native data get and set operations with ASM and JVM stdlib.
 *
 * @since 0.995.0
 */
public class ASMUtil {

    public static final String CLASS_WRITER = "ClassWriter";
    public static final String METHOD_VISITOR = "MethodVisitor";
    public static final String FIELD_VISITOR = "FieldVisitor";
    public static final String LABEL = "Label";
    public static final String JVM_PKG_PATH = BALLERINA_PACKAGE_PREFIX + "jvm";
    public static final BPackage JVM_PKG_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "jvm", "0.0.0");
    public static final String NATIVE_KEY = "native";
    public static final String INTEROP_VALIDATOR = "InteropValidator";

    public static final String OBJECT_DESC = "Ljava/lang/Object;";
    public static final String FUNCTION_DESC = "Ljava/util/function/Function;";
    public static final String STRING_DESC = "Ljava/lang/String;";
    public static final String METHOD_TYPE_DESC = "Ljava/lang/invoke/MethodType;";
    public static final String MAP_VALUE_DESC = "Lorg/ballerinalang/jvm/values/MapValue;";

    public static ObjectValue newObject(String type) {
        return BallerinaValues.createObjectValue(JVM_PKG_ID, type);
    }

    public static <T> T getRefArgumentNativeData(ObjectValue objectValue) {
        return (T) objectValue.getNativeData().get(NATIVE_KEY);
    }

    public static void addNativeDataToObject(Object data, ObjectValue objectValue) {
        objectValue.addNativeData(NATIVE_KEY, data);
    }

    public static String[] fromNilableStringArray(Object optArray) {
        return optArray == null ? null : ((ArrayValue) optArray).getStringArray();
    }
}
