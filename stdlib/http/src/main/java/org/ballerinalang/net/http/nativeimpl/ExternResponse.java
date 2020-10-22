/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl;

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.HttpUtil;

/**
 * Utilities related to HTTP response.
 *
 * @since 1.1.0
 */
public class ExternResponse {

    public static BObject createNewEntity(BObject responseObj) {
        return HttpUtil.createNewEntity(responseObj);
    }

    public static Object getEntity(BObject responseObj) {
        return HttpUtil.getEntity(responseObj, false, true, true);
    }

    public static BObject getEntityWithoutBodyAndHeaders(BObject responseObj) {
        return HttpUtil.getEntity(responseObj, false, false, false);
    }

    public static BObject getEntityWithBodyAndWithoutHeaders(BObject requestObj) {
        return HttpUtil.getEntity(requestObj, false, true, false);
    }

    public static void setEntity(BObject requestObj, BObject entityObj) {
        HttpUtil.setEntity(requestObj, entityObj, false, true);
    }

    public static void setEntityAndUpdateContentTypeHeader(BObject requestObj, BObject entityObj) {
        HttpUtil.setEntity(requestObj, entityObj, false, false);
    }
}
