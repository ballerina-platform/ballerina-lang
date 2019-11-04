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
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.nativeimpl.jvm.ASMUtil.INTEROP_VALIDATOR;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.CLASS_LOADER_DATA;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.IS_INTERFACE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.IS_STATIC_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.KIND_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.METHOD_THROWS_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.METHOD_TYPE_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.METHOD_TYPE_NAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.NAME_FIELD;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.SIG_FIELD;

/**
 * Ballerina external function implementation that validates Java interop functions and link them with Java methods.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = INTEROP_VALIDATOR, structPackage = JVM_PKG_PATH),
        functionName = "validateAndGetJMethod"
)
public class JInteropMethodValidator {

    public static Object validateAndGetJMethod(Strand strand, ObjectValue interopValidatorStruct,
                                               MapValue<String, Object> jMethodReqBValue) {
        try {
            ClassLoader classLoader = (ClassLoader) interopValidatorStruct.getNativeData(CLASS_LOADER_DATA);
            // Populate JMethodRequest from the BValue
            JMethodRequest jMethodRequest = JMethodRequest.build(jMethodReqBValue, classLoader);
            // Validate the Ballerina external function signature with the specific Java interoperability annotation
            validateBExternalFunction(jMethodRequest);
            // Find the most specific Java method or constructor for the given request
            JMethod jMethod = resolveJMethod(jMethodRequest, classLoader);
            // Return the matched Java method or constructor details back to the Ballerina land.
            return createJMethodBValue(jMethod, classLoader);
        } catch (JInteropException e) {
            return JInterop.createErrorBValue(e.getReason(), e.getMessage());
        }
    }

    private static void validateBExternalFunction(JMethodRequest jMethodRequest) {
    }

    private static JMethod resolveJMethod(JMethodRequest jMethodRequest, ClassLoader classLoader) {
        JMethodResolver methodResolver = new JMethodResolver(classLoader);
        return methodResolver.resolve(jMethodRequest);
    }

    private static MapValue createJMethodBValue(JMethod jMethod, ClassLoader classLoader) {
        MapValue<String, Object> jMethodRecord = JInterop.createRecordBValue(METHOD_TYPE_NAME);
        jMethodRecord.put(NAME_FIELD, jMethod.getName());
        jMethodRecord.put(CLASS_FIELD, jMethod.getClassName().replace('.', '/'));
        jMethodRecord.put(IS_INTERFACE_FIELD, jMethod.isDeclaringClassInterface());
        jMethodRecord.put(KIND_FIELD, jMethod.getKind().getStringValue());
        jMethodRecord.put(IS_STATIC_FIELD, jMethod.isStatic());
        jMethodRecord.put(SIG_FIELD, jMethod.getSignature());
        jMethodRecord.put(METHOD_TYPE_FIELD, JInterop.createJMethodTypeBValue(jMethod));
        jMethodRecord.put(METHOD_THROWS_FIELD, jMethod.getExceptionTypes(classLoader));
        return jMethodRecord;
    }
}

