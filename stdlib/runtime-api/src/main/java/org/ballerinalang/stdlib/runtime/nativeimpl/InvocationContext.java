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

import org.ballerinalang.jvm.values.MapValue;

/**
 * InvocationContext represents and holds the current invocation information.
 *
 * @since 0.970.0
 */
public class InvocationContext {

    public static final String INVOCATION_ID_KEY = "id";
    private MapValue<String, Object> invocationContextRecord;
    private UserPrincipal userPrincipal;
    private AuthenticationContext authenticationContext;

    public InvocationContext(MapValue<String, Object> invocationContextRecord, UserPrincipal userPrincipal,
                             AuthenticationContext authenticationContext) {
        this.invocationContextRecord = invocationContextRecord;
        this.authenticationContext = authenticationContext;
        this.userPrincipal = userPrincipal;
    }

    public MapValue<String, Object> getInvocationContextRecord() {
        return invocationContextRecord;
    }

    public String getId() {
        return invocationContextRecord.get(INVOCATION_ID_KEY).toString();
    }

    public void setId(String id) {
        invocationContextRecord.put(INVOCATION_ID_KEY, id);
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public UserPrincipal getUserPrincipal() {
        return userPrincipal;
    }
}
