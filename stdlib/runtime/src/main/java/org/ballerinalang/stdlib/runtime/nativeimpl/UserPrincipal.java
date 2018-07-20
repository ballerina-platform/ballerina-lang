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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;

import java.util.Map;

/**
 * UserPrincipal represents and holds the authenticated user information.
 *
 * @since 0.970.0
 */
public class UserPrincipal {

    public static final String USER_ID_STRING_FIELD_KEY = "userId";
    public static final String USER_NAME_STRING_FIELD_KEY = "username";
    public static final String CLAIMS_REF_FIELD_KEY = "claims";
    public static final String SCOPES_REF_FIELD_KEY = "scopes";

    private BMap<String, BValue> authContextStruct;

    public UserPrincipal(BMap<String, BValue> authContextStruct) {
        this.authContextStruct = authContextStruct;
    }

    public String getUserId() {
        return authContextStruct.get(USER_ID_STRING_FIELD_KEY).stringValue();
    }

    public void setUserId(String userId) {
        authContextStruct.put(USER_ID_STRING_FIELD_KEY, new BString(userId));
    }

    public String getUsername() {
        return authContextStruct.get(USER_NAME_STRING_FIELD_KEY).stringValue();
    }

    public void setUsername(String username) {
        authContextStruct.put(USER_NAME_STRING_FIELD_KEY, new BString(username));
    }

    public Map<String, String> getClaims() {
        return getMapField(authContextStruct.get(CLAIMS_REF_FIELD_KEY));
    }

    public void setClaims(Map<String, String> claims) {
        BMap<String, BString> bMap = new BMap<>();
        if (claims != null) {
            claims.forEach((key, value) -> bMap.put(key, new BString(value)));
        }
        authContextStruct.put(CLAIMS_REF_FIELD_KEY, bMap);
    }

    public String[] getScopes() {
        return getStringArrayField(authContextStruct.get(SCOPES_REF_FIELD_KEY));
    }

    public void setScopes(String[] scopes) {
        authContextStruct.put(SCOPES_REF_FIELD_KEY, new BStringArray(scopes));
    }

    public static String[] getStringArrayField(BValue bValue) {
        if (bValue != null && bValue instanceof BStringArray) {
            BStringArray bArray = (BStringArray) bValue;
            return bArray.getStringArray();
        }
        return new String[0];
    }

    public static Map<String, String> getMapField(BValue bValue) {
        if (bValue != null && bValue instanceof BMap) {
            BMap bMap = (BMap) bValue;
            return bMap.getMap();
        }
        return null;
    }
}
