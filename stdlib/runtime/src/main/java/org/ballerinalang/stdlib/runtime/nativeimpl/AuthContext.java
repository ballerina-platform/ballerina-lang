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

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

/**
 * AuthContext represents and holds the authentication information.
 *
 * @since 0.970.0
 */
public class AuthContext {

    public static final String AUTH_SCHEME_KEY = "scheme";
    public static final String AUTH_TOKEN_KEY = "authToken";

    private BMap<String, BValue> authContextStruct;

    public AuthContext(BMap<String, BValue> authContextStruct) {
        this.authContextStruct = authContextStruct;
    }

    public String getScheme() {
        return authContextStruct.get(AUTH_SCHEME_KEY).stringValue();
    }

    public void setScheme(String authType) {
        authContextStruct.put(AUTH_SCHEME_KEY, new BString(authType));
    }

    public String getAuthToken() {
        return authContextStruct.get(AUTH_TOKEN_KEY).stringValue();
    }

    public void setAuthToken(String authToken) {
        authContextStruct.put(AUTH_TOKEN_KEY, new BString(authToken));
    }
}
