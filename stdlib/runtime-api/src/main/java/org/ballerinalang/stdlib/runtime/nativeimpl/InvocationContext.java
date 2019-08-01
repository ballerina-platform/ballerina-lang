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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

/**
 * InvocationContext represents and holds the current invocation information.
 *
 * @since 0.970.0
 */
public class InvocationContext {

    public static final String INVOCATION_ID_KEY = "id";
    private MapValue<String, Object> invocationContextRecord;
    private BMap<String, BValue> invocationContextStruct;
    private UserPrincipal userPrincipal;
    private AuthenticationContext authenticationContext;

    //TODO Remove after migration : implemented using bvm values/types
    public InvocationContext(BMap<String, BValue> invocationContextStruct, UserPrincipal userPrincipal,
            AuthenticationContext authenticationContext) {
        this.invocationContextStruct = invocationContextStruct;
        this.authenticationContext = authenticationContext;
        this.userPrincipal = userPrincipal;
    }

    public InvocationContext(MapValue<String, Object> invocationContextRecord, UserPrincipal userPrincipal,
                             AuthenticationContext authenticationContext) {
        this.invocationContextRecord = invocationContextRecord;
        this.authenticationContext = authenticationContext;
        this.userPrincipal = userPrincipal;
    }

    //TODO Remove after migration : implemented using bvm values/types
    public BMap<String, BValue> getInvocationContextStruct() {
        return invocationContextStruct;
    }

    public MapValue<String, Object> getInvocationContextRecord() {
        return invocationContextRecord;
    }

    public String getId() {
        return invocationContextStruct.get(INVOCATION_ID_KEY).stringValue();
    }

    public void setId(String id) {
        invocationContextStruct.put(INVOCATION_ID_KEY, new BString(id));
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public UserPrincipal getUserPrincipal() {
        return userPrincipal;
    }
}
