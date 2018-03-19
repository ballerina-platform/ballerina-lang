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
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class contains the common constants and methods required for authentication context processing.
 *
 * @since 0.965.0
 */
public class AuthenticationContextUtils {

    public static final String AUTHENTICATION_CONTEXT_PROPERTY = "AuthenticationContext";

    public static AuthenticationContext getAuthenticationContext(Context context) {
        AuthenticationContext authContext = (AuthenticationContext) context.getProperty(AuthenticationContextUtils
                .AUTHENTICATION_CONTEXT_PROPERTY);
        if (authContext == null) {
            authContext = new AuthenticationContext();
            context.setProperty(AuthenticationContextUtils.AUTHENTICATION_CONTEXT_PROPERTY, authContext);
        }
        return authContext;
    }

    public static String[] getStringArrayField(BValue bValue) {
        if (bValue != null && bValue instanceof BStringArray) {
            BStringArray bArray = (BStringArray) bValue;
            String[] array = new String[(int) bArray.size()];
            for (int i = 0; i < (int) bArray.size(); i++) {
                array[i] = bArray.get(i);
            }
            return array;
        }
        return new String[0];
    }

    public static Map<String, String> getMapField(BValue bValue) {
        if (bValue != null && bValue instanceof BMap) {
            BMap bMap = (BMap) bValue;
            Map<String, String> valueMap = new LinkedHashMap<>();
            final BIterator bIterator = bMap.newIterator();
            while (bIterator.hasNext()) {
                final BValue[] next = bIterator.getNext(2);
                valueMap.put(next[0].stringValue(), next[1].toString());
            }
            return valueMap;
        }
        return null;
    }
}
