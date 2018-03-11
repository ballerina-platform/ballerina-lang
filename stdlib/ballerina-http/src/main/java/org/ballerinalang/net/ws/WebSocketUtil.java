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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.WebSocketConstants;

/**
 * Utility class for websockets.
 */
public abstract class WebSocketUtil {

    public static BValue[] getQueryParams(Context context) {
        BStruct wsConnection = (BStruct) context.getRefArgument(0);
        Object queryParams = wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS);
        if (queryParams != null && queryParams instanceof BMap) {
            return new BValue[] { (BMap) wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS) };
        }
        return new BValue[] { new BMap<>() };
    }
}
