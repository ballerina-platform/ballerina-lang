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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
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
    public static final String STRUCT_TYPE_AUTH_CONTEXT = "AuthContext";
    public static final String STRUCT_TYPE_USER_PRINCIPAL = "UserPrincipal";

    public static InvocationContext getInvocationContext(Context context) {
        InvocationContext invocationContext = (InvocationContext) context.getProperty(InvocationContextUtils
                .INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            synchronized (InvocationContextUtils.class) {
                if (context.getProperty(InvocationContextUtils
                        .INVOCATION_CONTEXT_PROPERTY) == null) {
                    invocationContext = initInvocationContext(context);
                    context.setProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY, invocationContext);
                }
            }
        }
        return invocationContext;
    }

    public static BMap<String, BValue> getInvocationContextStruct(Context context) {
        return getInvocationContext(context).getInvocationContextStruct();
    }

    private static InvocationContext initInvocationContext(Context context) {
        BMap<String, BValue> userPrincipalStruct = createUserPrincipal(context);
        UserPrincipal userPrincipal = new UserPrincipal(userPrincipalStruct);
        BMap<String, BValue> authContextStruct = createAuthContext(context);
        AuthContext authContext = new AuthContext(authContextStruct);
        BMap<String, BValue> invocationContextStruct =
                createInvocationContext(context, userPrincipalStruct, authContextStruct);
        InvocationContext invocationContext = new InvocationContext(
                invocationContextStruct, userPrincipal, authContext);
        return invocationContext;
    }

    private static StructureTypeInfo getStructInfo(Context context, String packageName, String structName) {
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(packageName);
        if (packageInfo == null) {
            return null;
        }
        return packageInfo.getStructInfo(structName);
    }

    private static BMap<String, BValue> createInvocationContext(Context context, BMap<String, BValue> userPrincipal,
                                                                BMap<String, BValue> authContext) {
        StructureTypeInfo invocationContextInfo = getStructInfo(context,
                BALLERINA_RUNTIME_PKG, STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        return BLangVMStructs.createBStruct(invocationContextInfo, invocationId.toString(), userPrincipal,
                authContext, new BMap());
    }

    private static BMap<String, BValue> createAuthContext(Context context) {
        StructureTypeInfo authContextInfo = getStructInfo(context, BALLERINA_RUNTIME_PKG, STRUCT_TYPE_AUTH_CONTEXT);
        String scheme = "";
        String authToken = "";
        return BLangVMStructs.createBStruct(authContextInfo, scheme, authToken);
    }

    private static BMap<String, BValue> createUserPrincipal(Context context) {
        StructureTypeInfo authContextInfo = getStructInfo(context, BALLERINA_RUNTIME_PKG, STRUCT_TYPE_USER_PRINCIPAL);
        String userId = "";
        String username = "";
        BMap<String, BString> claims = new BMap<>();
        BStringArray scopes = new BStringArray();
        return BLangVMStructs.createBStruct(authContextInfo, userId, username, claims, scopes);
    }
}
