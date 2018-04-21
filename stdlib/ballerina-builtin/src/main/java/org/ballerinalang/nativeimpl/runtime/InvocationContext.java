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

import org.ballerinalang.model.values.BStruct;

/**
 * InvocationContext represents and holds the current invocation information.
 *
 * @since 0.970.0
 */
public class InvocationContext {

    public static final int INVOCATION_ID_STRING_FIELD_INDEX = 0;
    private BStruct invocationContextStruct;
    private UserPrincipal userPrincipal;
    private AuthContext authContext;

    public InvocationContext(BStruct invocationContextStruct, UserPrincipal userPrincipal, AuthContext
            authenticationContext) {
        this.invocationContextStruct = invocationContextStruct;
        this.authContext = authenticationContext;
        this.userPrincipal = userPrincipal;
    }

    public BStruct getInvocationContextStruct() {
        return invocationContextStruct;
    }

    public String getId() {
        return invocationContextStruct.getStringField(INVOCATION_ID_STRING_FIELD_INDEX);
    }

    public void setId(String id) {
        invocationContextStruct.setStringField(INVOCATION_ID_STRING_FIELD_INDEX, id);
    }

    public AuthContext getAuthContext() {
        return authContext;
    }

    public UserPrincipal getUserPrincipal() {
        return userPrincipal;
    }
}
