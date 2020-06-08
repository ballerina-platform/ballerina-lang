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

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URIUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.QUERY_PARAM_MAP;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.HttpUtil.checkRequestBodySizeHeadersAvailability;

/**
 * Utilities related to HTTP request.
 *
 * @since 1.1.0
 */
public class ExternRequest {

    private static final BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));

    public static ObjectValue createNewEntity(ObjectValue requestObj) {
        return HttpUtil.createNewEntity(requestObj);
    }

    public static void setEntity(ObjectValue requestObj, ObjectValue entityObj) {
        HttpUtil.setEntity(requestObj, entityObj, true);
    }

    @SuppressWarnings("unchecked")
    public static MapValue<BString, Object> getQueryParams(ObjectValue requestObj) {
        try {
            Object queryParams = requestObj.getNativeData(QUERY_PARAM_MAP);
            if (queryParams != null) {
                return (MapValue<BString, Object>) queryParams;
            }
            HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) requestObj
                    .getNativeData(HttpConstants.TRANSPORT_MESSAGE);
            MapValue<BString, Object> params = new MapValueImpl<>(mapType);
            Object rawQueryString = httpCarbonMessage.getProperty(HttpConstants.RAW_QUERY_STR);
            if (rawQueryString != null) {
                URIUtil.populateQueryParamMap((String) rawQueryString, params);
            }
            requestObj.addNativeData(QUERY_PARAM_MAP, params);
            return params;
        } catch (Exception e) {
            throw HttpUtil.createHttpError("error while retrieving query param from message: " + e.getMessage(),
                                           HttpErrorType.GENERIC_LISTENER_ERROR);
        }
    }

    public static MapValue<BString, Object> getMatrixParams(ObjectValue requestObj, BString path) {
        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestObj, null);
        return URIUtil.getMatrixParamsMap(path.getValue(), httpCarbonMessage);
    }

    public static Object getEntity(ObjectValue requestObj) {
        return HttpUtil.getEntity(requestObj, true, true);
    }

    public static ObjectValue getEntityWithoutBody(ObjectValue requestObj) {
        return HttpUtil.getEntity(requestObj, true, false);
    }

    public static boolean checkEntityBodyAvailability(ObjectValue requestObj) {
        ObjectValue entityObj = (ObjectValue) requestObj.get(REQUEST_ENTITY_FIELD);
        return lengthHeaderCheck(requestObj) || EntityBodyHandler.checkEntityBodyAvailability(entityObj);
    }

    private static boolean lengthHeaderCheck(ObjectValue requestObj) {
        Object outboundMsg = requestObj.getNativeData(TRANSPORT_MESSAGE);
        if (outboundMsg == null) {
            return false;
        }
        return checkRequestBodySizeHeadersAvailability((HttpCarbonMessage) outboundMsg);
    }
}
