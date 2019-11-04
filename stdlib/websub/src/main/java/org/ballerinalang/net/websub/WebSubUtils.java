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
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.http.HttpUtil.populateEntityBody;
import static org.ballerinalang.net.http.HttpUtil.populateInboundRequest;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ERROR_DETAIL_RECORD;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_ID;

/**
 * Util class for WebSub.
 */
public class WebSubUtils {

    public static final String WEBSUB_ERROR_CODE = "{ballerina/websub}WebSubError";

    static ObjectValue getHttpRequest(HttpCarbonMessage httpCarbonMessage) {
        ObjectValue httpRequest = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, REQUEST);
        ObjectValue inRequestEntity = BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);

        populateInboundRequest(httpRequest, inRequestEntity, httpCarbonMessage);
        populateEntityBody(httpRequest, inRequestEntity, true, true);
        return httpRequest;
    }

    // TODO: 8/1/18 Handle duplicate code
    @SuppressWarnings("unchecked")
    static MapValue<String, ?> getJsonBody(ObjectValue httpRequest) {
        ObjectValue entityObj = extractEntity(httpRequest);
        if (entityObj != null) {
            Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            String stringPayload;
            if (dataSource != null) {
                stringPayload = MimeUtil.getMessageAsString(dataSource);
            } else {
                stringPayload = EntityBodyHandler.constructStringDataSource(entityObj);
                EntityBodyHandler.addMessageDataSource(entityObj, stringPayload);
                // Set byte channel to null, once the message data source has been constructed
                entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
            }

            Object result = JSONParser.parse(stringPayload);
            if (result instanceof MapValue) {
                return (MapValue<String, ?>) result;
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
     * Create WebSub specific error record with '{ballerina/websub}WebSubError' as error code.
     *
     * @param errMsg  Actual error message
     * @return Ballerina error value
     */
    public static ErrorValue createError(String errMsg) {
        return BallerinaErrors.createError(WEBSUB_ERROR_CODE, errMsg);
    }

    /**
     * Create WebSub specific error for a given error reason and detail.
     *
     * @param reason  The standard error reason
     * @param message  The Actual error cause
     * @return Ballerina error value
     */
    public static ErrorValue createError(String reason, String message) {
        Map<String, Object> values = new HashMap<>();
        values.put(BallerinaErrors.ERROR_MESSAGE_FIELD, message);
        MapValue<String, Object> detail =
                BallerinaValues.createRecordValue(WEBSUB_PACKAGE_ID, ERROR_DETAIL_RECORD, values);
        return BallerinaErrors.createError(reason, detail);
    }
}
