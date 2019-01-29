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

import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.http.HttpUtil.populateEntityBody;
import static org.ballerinalang.net.http.HttpUtil.populateInboundRequest;

/**
 * Util class for WebSub.
 */
class WebSubUtils {

    static BMap<String, BValue> getHttpRequest(ProgramFile programFile, HttpCarbonMessage httpCarbonMessage) {
        BMap<String, BValue> httpRequest = createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, REQUEST);
        BMap<String, BValue> inRequestEntity = createBStruct(programFile, PROTOCOL_PACKAGE_MIME, ENTITY);
        BMap<String, BValue> mediaType = createBStruct(programFile, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
        populateInboundRequest(httpRequest, inRequestEntity, mediaType, httpCarbonMessage, programFile);
        populateEntityBody(null, httpRequest, inRequestEntity, true);
        return httpRequest;
    }

    // TODO: 8/1/18 Handle duplicate code
    @SuppressWarnings("unchecked")
    static BMap<String, ?> getJsonBody(BMap<String, BValue> httpRequest) {
        BMap<String, BValue> entityStruct = extractEntity(httpRequest);
        if (entityStruct != null) {
            BValue dataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            BRefType<?> result;
            BString stringPayload;
            if (dataSource != null) {
                stringPayload = MimeUtil.getMessageAsString(dataSource);
            } else {
                stringPayload = EntityBodyHandler.constructStringDataSource(entityStruct);
                EntityBodyHandler.addMessageDataSource(entityStruct, stringPayload);
                // Set byte channel to null, once the message data source has been constructed
                entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
            }

            result = JsonParser.parse(stringPayload.stringValue());
            if (result instanceof BMap) {
                return (BMap<String, ?>) result;
            }
            throw new BallerinaConnectorException("Non-compatible payload received for payload key based dispatching");
        } else {
            throw new BallerinaConnectorException("Error retrieving payload for payload key based dispatching");
        }
    }

    private static BMap<String, BValue> createBStruct(ProgramFile programFile, String packagePath, String structName) {
        return BLangConnectorSPIUtil.createBStruct(programFile, packagePath, structName);
    }
}
