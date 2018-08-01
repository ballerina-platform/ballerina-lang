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
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
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
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
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

    public static HashMap<String, String[]> retrieveResourceDetails(WebSubServicesRegistry serviceRegistry) {
        //Map with resource details where the key is the resource name and the value is the param
        HashMap<String, String[]> resourceDetails = new HashMap<>();
        resourceDetails.put(RESOURCE_NAME_ON_INTENT_VERIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST});
        resourceDetails.put(RESOURCE_NAME_ON_NOTIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST});

        String topicIdentifier = serviceRegistry.getTopicIdentifier();
        if (topicIdentifier != null) {
            switch (topicIdentifier) {
                case TOPIC_ID_HEADER:
                    populateResourceDetailsByHeader(serviceRegistry.getHeaderResourceMap(), resourceDetails);
                    break;
                case TOPIC_ID_PAYLOAD_KEY:
                    populateResourceDetailsByPayload(serviceRegistry.getPayloadKeyResourceMap(), resourceDetails);
                    break;
                default:
                    populateResourceDetailsByHeaderAndPayload(serviceRegistry.getHeaderAndPayloadKeyResourceMap(),
                                                              resourceDetails);
                    if (serviceRegistry.getHeaderResourceMap() != null) {
                        populateResourceDetailsByHeader(serviceRegistry.getHeaderResourceMap(), resourceDetails);
                    }
                    if (serviceRegistry.getPayloadKeyResourceMap() != null) {
                        populateResourceDetailsByPayload(serviceRegistry.getPayloadKeyResourceMap(), resourceDetails);
                    }
                    break;
            }
        }
        return resourceDetails;
    }

    private static void populateResourceDetailsByHeader(BMap<String, BValue> headerResourceMap,
                                                        HashMap<String, String[]> resourceDetails) {
        headerResourceMap.getMap().values().forEach(value -> {
            BRefValueArray resourceDetailTuple = (BRefValueArray) value;
            String resourceName = resourceDetailTuple.getBValue(0).stringValue();
            BStructureType paramDetails =
                    (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
            resourceDetails.put(resourceName,
                                new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
        });
    }

    private static void populateResourceDetailsByPayload(BMap<String, BMap<String, BValue>> payloadKeyResourceMap,
                                                         HashMap<String, String[]> resourceDetails) {
        payloadKeyResourceMap.getMap().values().forEach(mapByKey -> {
            mapByKey.getMap().values().forEach(value -> {
                BRefValueArray resourceDetailTuple = (BRefValueArray) value;
                String resourceName = resourceDetailTuple.getBValue(0).stringValue();
                BStructureType paramDetails =
                        (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
                resourceDetails.put(resourceName,
                                    new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
            });
        });
    }

    private static void populateResourceDetailsByHeaderAndPayload(BMap<String, BMap<String, BMap<String, BValue>>>
                                                                          headerAndPayloadKeyResourceMap,
                                                                  HashMap<String, String[]> resourceDetails) {
        headerAndPayloadKeyResourceMap.getMap().values().forEach(mapByHeader -> {
            mapByHeader.getMap().values().forEach(mapByKey -> {
                mapByKey.getMap().values().forEach(value -> {
                    BRefValueArray resourceDetailTuple = (BRefValueArray) value;
                    String resourceName = resourceDetailTuple.getBValue(0).stringValue();
                    BStructureType paramDetails =
                            (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
                    resourceDetails.put(resourceName,
                                        new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
                });
            });
        });
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

    // TODO: 8/1/18 Handle duplicate code
    public static BMap<String, ?> getJsonBody(BMap<String, BValue> httpRequest) {
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
