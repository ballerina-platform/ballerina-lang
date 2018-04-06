/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.UUID;

/**
 * This class contains the common constants and methods required for invocation context processing.
 *
 * @since 0.970.0
 */
public class InvocationContextUtils {

    public static final String INVOCATION_CONTEXT_PROPERTY = "InvocationContext";
    public static final String PACKAGE_RUNTIME = "ballerina.runtime";
    public static final String STRUCT_TYPE_INVOCATION_CONTEXT = "InvocationContext";
    public static final String STRUCT_TYPE_AUTHENTICATION_CONTEXT = "AuthenticationContext";

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

    public static BStruct getInvocationContextStruct(Context context) {
        return getInvocationContext(context).getInvocationContextStruct();
    }

    private static InvocationContext initInvocationContext(Context context) {
        BStruct authContextStruct = createAuthContext(context);
        AuthenticationContext authContext = new AuthenticationContext(authContextStruct);
        BStruct invocationContextStruct = createInvocationContext(context, authContextStruct);
        InvocationContext invocationContext = new InvocationContext(invocationContextStruct, authContext);
        return invocationContext;
    }

    private static StructInfo getStructInfo(Context context, String packageName, String structName) {
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(packageName);
        if (packageInfo == null) {
            return null;
        }
        return packageInfo.getStructInfo(structName);
    }

    private static BStruct createInvocationContext(Context context, BStruct authContext) {
        StructInfo invocationContextInfo = getStructInfo(context, PACKAGE_RUNTIME, STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        return BLangVMStructs.createBStruct(invocationContextInfo, invocationId.toString(), authContext);
    }

    private static BStruct createAuthContext(Context context) {
        StructInfo authContextInfo = getStructInfo(context, PACKAGE_RUNTIME, STRUCT_TYPE_AUTHENTICATION_CONTEXT);
        String userId = "";
        String username = "";
        BStringArray groups = new BStringArray();
        BMap<String, BString> claims = new BMap<>();
        BStringArray scopes = new BStringArray();
        String authType = "";
        String authToken = "";
        return BLangVMStructs.createBStruct(authContextInfo, userId, username,
                groups, claims, scopes, authType, authToken);
    }
}
