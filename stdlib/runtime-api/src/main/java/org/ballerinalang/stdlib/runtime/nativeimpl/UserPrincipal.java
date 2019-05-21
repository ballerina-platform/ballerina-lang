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

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;

import java.util.Map;

/**
 * UserPrincipal represents and holds the authenticated user information.
 *
 * @since 0.970.0
 */
public class UserPrincipal {

    private static final String USER_ID_STRING_FIELD_KEY = "userId";
    private static final String USER_NAME_STRING_FIELD_KEY = "username";
    private static final String CLAIMS_REF_FIELD_KEY = "claims";
    private static final String SCOPES_REF_FIELD_KEY = "scopes";
    private MapValue<String, Object> authContextRecord = null;
    private BMap<String, BValue> authContextStruct = null;

    //TODO Remove after migration : implemented using bvm values/types
    public UserPrincipal(BMap<String, BValue> authContextStruct) {
        this.authContextStruct = authContextStruct;
    }

    public UserPrincipal(MapValue<String, Object> authContextRecord) {
        this.authContextRecord = authContextRecord;
    }

    public String getUserId() {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            return authContextStruct.get(USER_ID_STRING_FIELD_KEY).stringValue();
        }
        return authContextRecord.get(USER_ID_STRING_FIELD_KEY).toString();
    }

    public void setUserId(String userId) {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            authContextStruct.put(USER_ID_STRING_FIELD_KEY, new BString(userId));
        } else {
            authContextRecord.put(USER_ID_STRING_FIELD_KEY, userId);
        }
    }

    public String getUsername() {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            return authContextStruct.get(USER_NAME_STRING_FIELD_KEY).stringValue();
        }
        return authContextRecord.get(USER_NAME_STRING_FIELD_KEY).toString();
    }

    public void setUsername(String username) {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            authContextStruct.put(USER_NAME_STRING_FIELD_KEY, new BString(username));
        } else {
            authContextRecord.put(USER_NAME_STRING_FIELD_KEY, username);
        }
    }

    public Map<String, String> getClaims() {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            return getMapField(authContextStruct.get(CLAIMS_REF_FIELD_KEY));
        }
        return getMapField(authContextRecord.get(CLAIMS_REF_FIELD_KEY));
    }

    public void setClaims(Map<String, String> claims) {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            BMap<String, BString> bMap = new BMap<>();
            if (claims != null) {
                claims.forEach((key, value) -> bMap.put(key, new BString(value)));
            }
            authContextStruct.put(CLAIMS_REF_FIELD_KEY, bMap);
        } else {
            MapValue<String, String> mapValue = new MapValueImpl<>();
            if (claims != null) {
                claims.forEach(mapValue::put);
            }
            authContextRecord.put(CLAIMS_REF_FIELD_KEY, mapValue);
        }
    }

    public String[] getScopes() {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            return getStringArrayField(authContextStruct.get(SCOPES_REF_FIELD_KEY));
        }
        return getStringArrayField(authContextRecord.get(SCOPES_REF_FIELD_KEY));
    }

    public void setScopes(String[] scopes) {
        //TODO this check is to distinguish both bvm and jvm values. Remove after the migration
        if (authContextStruct != null) {
            authContextStruct.put(SCOPES_REF_FIELD_KEY, new BValueArray(scopes));
        } else {
            authContextRecord.put(SCOPES_REF_FIELD_KEY, new ArrayValue(scopes));
        }
    }

    //TODO Remove after migration : implemented using bvm values/types
    public static String[] getStringArrayField(BValue bValue) {
        if (bValue != null && bValue instanceof BValueArray) {
            BValueArray bArray = (BValueArray) bValue;
            return bArray.getStringArray();
        }
        return new String[0];
    }

    private static String[] getStringArrayField(Object value) {
        if (value != null && value instanceof ArrayValue) {
            ArrayValue bArray = (ArrayValue) value;
            return bArray.getStringArray();
        }
        return new String[0];
    }

    //TODO Remove after migration : implemented using bvm values/types
    public static Map<String, String> getMapField(BValue bValue) {
        if (bValue != null && bValue instanceof BMap) {
            BMap bMap = (BMap) bValue;
            return bMap.getMap();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static MapValue<String, String> getMapField(Object value) {
        if (value != null && value instanceof MapValue) {
            return (MapValue) value;
        }
        return null;
    }
}
