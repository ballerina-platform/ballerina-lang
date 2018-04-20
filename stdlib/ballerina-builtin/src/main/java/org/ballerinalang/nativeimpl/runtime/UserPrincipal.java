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

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;

import java.util.Map;

/**
 * UserPrincipal represents and holds the authenticated user information.
 *
 * @since 0.970.0
 */
public class UserPrincipal {

    public static final int USER_ID_STRING_FIELD_INDEX = 0;
    public static final int USER_NAME_STRING_FIELD_INDEX = 1;
    public static final int CLAIMS_REF_FIELD_INDEX = 0;
    public static final int SCOPES_REF_FIELD_INDEX = 1;

    private BStruct authContextStruct;

    public UserPrincipal(BStruct authContextStruct) {
        this.authContextStruct = authContextStruct;
    }

    public String getUserId() {
        return authContextStruct.getStringField(USER_ID_STRING_FIELD_INDEX);
    }

    public void setUserId(String userId) {
        authContextStruct.setStringField(USER_ID_STRING_FIELD_INDEX, userId);
    }

    public String getUsername() {
        return authContextStruct.getStringField(USER_NAME_STRING_FIELD_INDEX);
    }

    public void setUsername(String username) {
        authContextStruct.setStringField(USER_NAME_STRING_FIELD_INDEX, username);
    }

    public Map<String, String> getClaims() {
        return getMapField(authContextStruct.getRefField(CLAIMS_REF_FIELD_INDEX));
    }

    public void setClaims(Map<String, String> claims) {
        BMap<String, BString> bMap = new BMap<>();
        if (claims != null) {
            claims.forEach((key, value) -> bMap.put(key, new BString(value)));
        }
        authContextStruct.setRefField(CLAIMS_REF_FIELD_INDEX, bMap);
    }

    public String[] getScopes() {
        return getStringArrayField(authContextStruct.getRefField(SCOPES_REF_FIELD_INDEX));
    }

    public void setScopes(String[] scopes) {
        authContextStruct.setRefField(SCOPES_REF_FIELD_INDEX, new BStringArray(scopes));
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
