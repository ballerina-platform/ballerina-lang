/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Util class for WebSub.
 */
public class WebSubUtils {

    public static final String WEBSUB_ERROR = "WebSubError";

    static ObjectValue getHttpRequest(HttpCarbonMessage httpCarbonMessage) {
        ObjectValue httpRequest = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                                                                    HttpConstants.REQUEST);
        ObjectValue inRequestEntity = BallerinaValues.createObjectValue(MimeConstants.PROTOCOL_MIME_PKG_ID,
                                                                        MimeConstants.ENTITY);

        HttpUtil.populateInboundRequest(httpRequest, inRequestEntity, httpCarbonMessage);
        HttpUtil.populateEntityBody(httpRequest, inRequestEntity, true, true);
        return httpRequest;
    }

    // TODO: 8/1/18 Handle duplicate code
    @SuppressWarnings("unchecked")
    static MapValue<BString, ?> getJsonBody(ObjectValue httpRequest) {
        ObjectValue entityObj = HttpUtil.extractEntity(httpRequest);
        if (entityObj != null) {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            String stringPayload;
            if (dataSource != null) {
                stringPayload = MimeUtil.getMessageAsString(dataSource);
            } else {
                stringPayload = EntityBodyHandler.constructStringDataSource(entityObj).getValue();
                EntityBodyHandler.addMessageDataSource(entityObj, stringPayload);
                // Set byte channel to null, once the message data source has been constructed
                entityObj.addNativeData(MimeConstants.ENTITY_BYTE_CHANNEL, null);
            }

            Object result = JSONParser.parse(stringPayload);
            if (result instanceof MapValue) {
                return (MapValue<BString, ?>) result;
            }
            throw new BallerinaConnectorException("Non-compatible payload received for payload key based dispatching");
        } else {
            throw new BallerinaConnectorException("Error retrieving payload for payload key based dispatching");
        }
    }

    public static AttachedFunction getAttachedFunction(ObjectValue service, String functionName) {
        AttachedFunction attachedFunction = null;
        String functionFullName = service.getType().getName() + "." + functionName;
        for (AttachedFunction function : service.getType().getAttachedFunctions()) {
            //TODO test the name of resource
            if (functionFullName.contains(function.getName())) {
                attachedFunction = function;
            }
        }
        return attachedFunction;
    }

    /**
     * Create WebSub specific error record with 'WebSubError' as error type ID name.
     *
     * @param errMsg  Actual error message
     * @return Ballerina error value
     */
    public static ErrorValue createError(String errMsg) {
        return BallerinaErrors.createDistinctError(WEBSUB_ERROR, WebSubSubscriberConstants.WEBSUB_PACKAGE_ID,
                errMsg);
    }

    /**
     * Create WebSub specific error for a given error message.
     *
     * @param typeIdName  The error type ID name
     * @param message  The Actual error cause
     * @return Ballerina error value
     */
    public static ErrorValue createError(String typeIdName, String message) {
        return BallerinaErrors.createDistinctError(typeIdName, WebSubSubscriberConstants.WEBSUB_PACKAGE_ID, message);
    }
}
