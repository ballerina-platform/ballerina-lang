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
package org.ballerinalang.nativeimpl.jvm.interop;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.nativeimpl.jvm.interop.JavaField.JFieldMethod;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.lang.reflect.Field;

import static org.ballerinalang.nativeimpl.jvm.ASMUtil.INTEROP_VALIDATOR;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_LOADER_DATA;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.FIELD_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.IS_STATIC_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.METHOD_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.SIG_FIELD;

/**
 * Ballerina external function implementation that validates Java interop functions and link them with Java fields.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = INTEROP_VALIDATOR, structPackage = JVM_PKG_PATH),
        functionName = "validateAndGetJField"
)
public class JInteropFieldValidator {

    public static Object validateAndGetJField(Strand strand, ObjectValue interopValidatorStruct,
                                              MapValue<String, Object> jFieldValidationRequest) {
        try {
            // 1) Load Java class  - validate
            ClassLoader classLoader = (ClassLoader) interopValidatorStruct.getNativeData(CLASS_LOADER_DATA);
            JavaField.JFieldMethod method = getFieldMethod(jFieldValidationRequest);
            String className = (String) jFieldValidationRequest.get(CLASS_FIELD);
            Class clazz = JInterop.loadClass(className, classLoader);

            // 2) Load Java method details - use the method kind in the request - validate kind and the existance of the
            // method. Possible there may be more than one methods for the given kind and the name
            String fieldName = (String) jFieldValidationRequest.get(NAME_FIELD);
            JavaField javaField;
            try {
                Field field = clazz.getField(fieldName);
                javaField = new JavaField(method, field);
            } catch (NoSuchFieldException e) {
                throw new JInteropException(JInteropException.FIELD_NOT_FOUND_REASON,
                                            "No such field '" + fieldName + "' found in class '" + className + "'");
            }
            return createJFieldBValue(javaField);
        } catch (JInteropException e) {
            return JInterop.createErrorBValue(e.getReason(), e.getMessage());
        }
        // Any other exceptions will cause a panic.
    }

    private static JFieldMethod getFieldMethod(MapValue<String, Object> jFieldValidationRequest) {
        return JFieldMethod.getKind((String) jFieldValidationRequest.get(METHOD_FIELD));
    }

    private static MapValue<String, Object> createJFieldBValue(JavaField javaField) {
        MapValue<String, Object> jFieldBRecord = JInterop.createRecordBValue(JInterop.FIELD_TYPE_NAME);
        jFieldBRecord.put(NAME_FIELD, javaField.getName());
        jFieldBRecord.put(CLASS_FIELD, javaField.getDeclaringClassName().replace('.', '/'));
        jFieldBRecord.put(IS_STATIC_FIELD, javaField.isStatic());
        jFieldBRecord.put(METHOD_FIELD, javaField.getMethod().getStringValue());
        jFieldBRecord.put(SIG_FIELD, javaField.getSignature());
        jFieldBRecord.put(FIELD_TYPE_FIELD, JInterop.createJTypeBValue(javaField.getFieldType()));

        return jFieldBRecord;
    }
}


