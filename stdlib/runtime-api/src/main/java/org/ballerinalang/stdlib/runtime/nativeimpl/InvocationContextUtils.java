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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

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

    public static InvocationContext getInvocationContext(Strand strand) {
        InvocationContext invocationContext = (InvocationContext) strand.getProperty(InvocationContextUtils
                .INVOCATION_CONTEXT_PROPERTY);
        if (invocationContext == null) {
            invocationContext = initInvocationContext(strand);
            strand.setProperty(InvocationContextUtils.INVOCATION_CONTEXT_PROPERTY, invocationContext);
        }
        return invocationContext;
    }

    public static MapValue<String, Object> getInvocationContextRecord(Strand strand) {
        return getInvocationContext(strand).getInvocationContextRecord();
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

    private static MapValue<String, Object> createInvocationContext(MapValue<String, Object> userPrincipal,
                                                                    MapValue<String, Object> authContext) {
        MapValue<String, Object> invocationContextInfo = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG,
                                                                               STRUCT_TYPE_INVOCATION_CONTEXT);
        UUID invocationId = UUID.randomUUID();
        return BallerinaValues.createRecord(invocationContextInfo, invocationId.toString(), userPrincipal,
                                            authContext, new MapValueImpl());
    }

    private static MapValue<String, Object> createAuthenticationContext() {
        MapValue<String, Object> authContextInfo = BallerinaValues.createRecordValue(BALLERINA_RUNTIME_PKG,
                                                                           STRUCT_TYPE_AUTHENTICATION_CONTEXT);
        String scheme = "";
        String authToken = "";
        return BallerinaValues.createRecord(authContextInfo, scheme, authToken);
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
