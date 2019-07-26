/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.UUID;

import static org.ballerinalang.util.BLangConstants.BALLERINA_RUNTIME_PKG;

/**
 * This class contains the common constants and methods required for invocation context processing.
 *
 * @since 0.970.0
 */
public class InvocationContextUtils {

    public static final String INVOCATION_CONTEXT_PROPERTY = "InvocationContext";
    public static final String STRUCT_TYPE_INVOCATION_CONTEXT = "InvocationContext";
    public static final String STRUCT_TYPE_AUTHENTICATION_CONTEXT = "AuthenticationContext";
    public static final String STRUCT_TYPE_PRINCIPAL = "Principal";

    //TODO Remove after migration : implemented using bvm values/types
    public static InvocationContext getInvocationContext(Context context) {
        InvocationContext invocationContext = (InvocationContext) context.getProperty(InvocationContextUtils
                .INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = initInvocationContext(context);
            context.setProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }

    public static InvocationContext getInvocationContext(Strand strand) {
        InvocationContext invocationContext = (InvocationContext) strand.getProperty(InvocationContextUtils
                .INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = initInvocationContext(strand);
            strand.setProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }

    //TODO Remove after migration : implemented using bvm values/types
    public static BMap<String, BValue> getInvocationContextStruct(Context context) {
        return getInvocationContext(context).getInvocationContextStruct();
    }

    public static MapValue<String, Object> getInvocationContextRecord(Strand strand) {
        return getInvocationContext(strand).getInvocationContextRecord();
    }

    //TODO Remove after migration : implemented using bvm values/types
    private static InvocationContext initInvocationContext(Context context) {
        BMap<String, BValue> userPrincipalStruct = createUserPrincipal(context);
        UserPrincipal userPrincipal = new UserPrincipal(userPrincipalStruct);
        BMap<String, BValue> authContextStruct = createAuthenticationContext(context);
        AuthenticationContext authenticationContext = new AuthenticationContext(authContextStruct);
        BMap<String, BValue> invocationContextStruct =
                createInvocationContext(context, userPrincipalStruct, authContextStruct);
        InvocationContext invocationContext = new InvocationContext(
                invocationContextStruct, userPrincipal, authenticationContext);
        return invocationContext;
    }

    private static InvocationContext initInvocationContext(Strand strand) {
        MapValue<String, Object> userPrincipalRecord = createUserPrincipal();
        UserPrincipal userPrincipal = new UserPrincipal(userPrincipalRecord);
        MapValue<String, Object> authContextRecord = createAuthenticationContext();
        AuthenticationContext authenticationContext = new AuthenticationContext(authContextRecord);
        MapValue<String, Object> invocationContextRecord = createInvocationContext(userPrincipalRecord,
                                                                                   authContextRecord);
        return new InvocationContext(invocationContextRecord, userPrincipal, authenticationContext);
    }

    private static StructureTypeInfo getStructInfo(Context context, String packageName, String structName) {
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(packageName);
        if (packageInfo == null) {
            return null;
        }
        return packageInfo.getStructInfo(structName);
    }

    //TODO Remove after migration : implemented using bvm values/types
    private static BMap<String, BValue> createInvocationContext(Context context, BMap<String, BValue> userPrincipal,
                                                                BMap<String, BValue> authContext) {
        StructureTypeInfo invocationContextInfo = getStructInfo(context,
                BALLERINA_RUNTIME_PKG, STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        return BLangVMStructs.createBStruct(invocationContextInfo, invocationId.toString(), userPrincipal,
                authContext, new BMap());
    }

    private static MapValue<String, Object> createInvocationContext(MapValue<String, Object> userPrincipal,
                                                                    MapValue<String, Object> authContext) {
        MapValue<String, Object> invocationContextInfo = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG,
                                                                               STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        return BallerinaValues.createRecord(invocationContextInfo, invocationId.toString(), userPrincipal,
                                            authContext, new MapValueImpl());
    }

    //TODO Remove after migration : implemented using bvm values/types
    private static BMap<String, BValue> createAuthenticationContext(Context context) {
        StructureTypeInfo authContextInfo = getStructInfo(context, BALLERINA_RUNTIME_PKG,
                STRUCT_TYPE_AUTHENTICATION_CONTEXT);
        String scheme = "";
        String authToken = "";
        return BLangVMStructs.createBStruct(authContextInfo, scheme, authToken);
    }

    private static MapValue<String, Object> createAuthenticationContext() {
        MapValue<String, Object> authContextInfo = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG,
                                                                           STRUCT_TYPE_AUTHENTICATION_CONTEXT);
        String scheme = "";
        String authToken = "";
        return BallerinaValues.createRecord(authContextInfo, scheme, authToken);
    }

    //TODO Remove after migration : implemented using bvm values/types
    private static BMap<String, BValue> createUserPrincipal(Context context) {
        StructureTypeInfo authContextInfo = getStructInfo(context, BALLERINA_RUNTIME_PKG, STRUCT_TYPE_PRINCIPAL);
        String userId = "";
        String username = "";
        BMap<String, BString> claims = new BMap<>();
        BValueArray scopes = new BValueArray(BTypes.typeString);
        return BLangVMStructs.createBStruct(authContextInfo, userId, username, claims, scopes);
    }

    private static MapValue<String, Object> createUserPrincipal() {
        MapValue<String, Object> authContextInfo = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG,
                                                                                     STRUCT_TYPE_PRINCIPAL);
        String userId = "";
        String username = "";
        MapValue<String, String> claims = new MapValueImpl<>();
        ArrayValue scopes = new ArrayValue(org.ballerinalang.jvm.types.BTypes.typeString);
        return BallerinaValues.createRecord(authContextInfo, userId, username, claims, scopes);
    }
}
