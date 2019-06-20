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
package org.ballerinalang.stdlib.utils;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.net.http.HttpConstants.ENTITY;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE;

/**
 * Utility functions to create JVM values.
 *
 * @since 1.0
 */
public class ValueCreatorUtils {

    public static ObjectValue createRequestObject() {
        ObjectValue request = BallerinaValues.createObjectValue(HTTP_PACKAGE_PATH, REQUEST);
        HttpCarbonMessage requestMsg = HttpUtil.getCarbonMsg(request, HttpUtil.createHttpCarbonMessage(true));
        HttpUtil.checkEntityAvailability(request);
        HttpUtil.enrichOutboundMessage(requestMsg, request);
        return request;
    }

    public static ObjectValue createResponseObject() {
        ObjectValue response = BallerinaValues.createObjectValue(HTTP_PACKAGE_PATH, RESPONSE);
        HttpUtil.checkEntityAvailability(response);
        return response;
    }


    public static ObjectValue createEntityObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, ENTITY);
    }

    public static ObjectValue createMediaTypeObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
    }
}
