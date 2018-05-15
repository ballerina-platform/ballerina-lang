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

package org.ballerinalang.net.websub.util;

import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.mime.util.EntityBodyHandler.addMessageDataSource;
import static org.ballerinalang.mime.util.EntityBodyHandler.getMessageDataSource;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.http.HttpUtil.populateEntityBody;
import static org.ballerinalang.net.http.HttpUtil.populateInboundRequest;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * Util class for WebSub.
 */
public class WebSubUtils {

    public static BMap<String, BValue> getHttpRequest(ProgramFile programFile, HttpCarbonMessage httpCarbonMessage) {
        BMap<String, BValue> httpRequest = createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, REQUEST);
        BMap<String, BValue> inRequestEntity = createBStruct(programFile, PROTOCOL_PACKAGE_MIME, ENTITY);
        BMap<String, BValue> mediaType = createBStruct(programFile, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
        populateInboundRequest(httpRequest, inRequestEntity, mediaType, httpCarbonMessage, programFile);
        populateEntityBody(null, httpRequest, inRequestEntity, true);
        return httpRequest;
    }

    public static HashMap<String, String[]> retrieveResourceDetails(BMap<String,
                                                                    BMap<String, BValue>> topicResourceMap) {
        //Map with resource details where the key is the resource name and the value is the param
        HashMap<String, String[]> resourceDetails = new HashMap<>();
        resourceDetails.put(RESOURCE_NAME_ON_INTENT_VERIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST});
        resourceDetails.put(RESOURCE_NAME_ON_NOTIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST});

        if (topicResourceMap != null) {
            for (String key : topicResourceMap.keySet()) {
                BMap<String, BValue> topicResourceSubMap = topicResourceMap.get(key);
                String resourceName;
                BStructureType paramDetails;
                if (TOPIC_ID_HEADER.equals(key)) {
                    for (String headerValue : topicResourceSubMap.keySet()) {
                        resourceName = (((BRefValueArray) topicResourceSubMap.get(headerValue)).getBValue(0))
                                .stringValue();
                        paramDetails = (BStructureType) ((BTypeDescValue) ((BRefValueArray)
                                                       topicResourceSubMap.get(headerValue)).getBValue(1)).value();
                        resourceDetails.put(resourceName,
                                            new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
                    }
                } else {
                    for (String topic : topicResourceSubMap.keySet()) {
                        BMap<String, BValue> topicResourceInternalMap =
                                (BMap<String, BValue>) topicResourceSubMap.get(topic);
                        for (String topicKey : topicResourceInternalMap.keySet()) {
                            resourceName = (((BRefValueArray) topicResourceInternalMap.get(topicKey))
                                                    .getBValue(0)).stringValue();
                            paramDetails = (BStructureType) ((BTypeDescValue) ((BRefValueArray)
                                                   topicResourceInternalMap.get(topicKey)).getBValue(1)).value();
                            resourceDetails.put(resourceName,
                                                new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
                        }
                    }
                }
            }
        }

        return resourceDetails;
    }

    public static void validateParamNumber(List<ParamDetail> paramDetails, int expectedSize, String resourceName) {
        if (paramDetails == null || paramDetails.size() < expectedSize) {
            throw new BallerinaException(String.format("Invalid resource signature for WebSub Resource \"%s\"",
                                                       resourceName));
        }
    }

    public static void validateStructType(String resourceName, ParamDetail paramDetail, String packageName,
                                   String structName) {
        if (!paramDetail.getVarType().getPackagePath().equals(packageName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramDetail.getVarType().getPackagePath(), paramDetail.getVarType().getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }

        if (!paramDetail.getVarType().getName().equals(structName)) {
            throw new BallerinaException(
                    String.format("Invalid parameter type %s:%s %s in resource %s. Requires %s:%s",
                                  paramDetail.getVarType().getPackagePath(), paramDetail.getVarType().getName(),
                                  paramDetail.getVarName(), resourceName, packageName, structName));
        }
    }

    public static BJSON getJsonBody(BMap<String, BValue> httpRequest) {
        BMap<String, BValue> entityStruct = extractEntity(httpRequest);
        MessageDataSource dataSource = getMessageDataSource(entityStruct);
        BJSON jsonBody = null;
        if (dataSource != null) {
            if (!(dataSource instanceof BJSON)) {
                // This would be the common case where the dataSource is an instance of StringDataSource due to
                // signature validation happening prior to reaching this point
                jsonBody = new BJSON(dataSource.getMessageAsString());
            } else {
                jsonBody = (BJSON) dataSource;
            }
        } else {
            addMessageDataSource(entityStruct, EntityBodyHandler.constructJsonDataSource(entityStruct));
            entityStruct.addNativeData(ENTITY_BYTE_CHANNEL, null);
            if (entityStruct.getNativeData(MESSAGE_DATA_SOURCE) instanceof BJSON) {
                jsonBody = (BJSON) (entityStruct.getNativeData(MESSAGE_DATA_SOURCE));
            } else {
                PrintStream console = System.err;
                console.println("ballerina: Non-JSON payload received as WebSub Notification");
            }

        }
        return jsonBody;
    }

    private static BMap<String, BValue> createBStruct(ProgramFile programFile, String packagePath, String structName) {
        return BLangConnectorSPIUtil.createBStruct(programFile, packagePath, structName);
    }

}
