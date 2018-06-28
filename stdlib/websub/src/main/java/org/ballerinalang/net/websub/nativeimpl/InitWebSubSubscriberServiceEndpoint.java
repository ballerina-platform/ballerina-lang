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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_TOPIC_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_TOPIC_IDENTIFIER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_TOPIC_PAYLOAD_KEYS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_TOPIC_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Initialize the WebSub subscriber endpoint.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "initWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = WEBSUB_PACKAGE)
)
public class InitWebSubSubscriberServiceEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = ((subscriberServiceEndpoint).getRefField(WEBSUB_HTTP_ENDPOINT).getStructValue());
        BMap<String, BValue> listener = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> config = (BMap<String, BValue>) listener.get(LISTENER_SERVICE_ENDPOINT_CONFIG);

        WebSubServicesRegistry webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry());
        BString topicIdentifier = (BString) config.get(SERVICE_CONFIG_TOPIC_IDENTIFIER);
        if (topicIdentifier != null) {
            String stringTopicIdentifier = topicIdentifier.stringValue();
            webSubServicesRegistry.setTopicIdentifier(stringTopicIdentifier);
            if (TOPIC_ID_HEADER.equals(stringTopicIdentifier)) {
                BString topicHeader = (BString) config.get(SERVICE_CONFIG_TOPIC_HEADER);
                if (topicHeader != null) {
                    webSubServicesRegistry.setTopicHeader(topicHeader.stringValue());
                } else {
                    throw new BallerinaConnectorException("Topic Header not specified to dispatch by Header");
                }
            } else if (TOPIC_ID_PAYLOAD_KEY.equals(stringTopicIdentifier)) {
                BStringArray topicPayloadKeys = (BStringArray) config.get(SERVICE_CONFIG_TOPIC_PAYLOAD_KEYS);
                if (topicPayloadKeys != null) {
                    webSubServicesRegistry.setTopicPayloadKeys(topicPayloadKeys);
                } else {
                    throw new BallerinaConnectorException("Payload Keys not specified to dispatch by Payload Key");
                }
            } else {
                BString topicHeader = (BString) config.get(SERVICE_CONFIG_TOPIC_HEADER);
                BStringArray topicPayloadKeys = (BStringArray) config.get(SERVICE_CONFIG_TOPIC_PAYLOAD_KEYS);
                if (topicHeader != null && topicPayloadKeys != null) {
                    webSubServicesRegistry.setTopicHeader(topicHeader.stringValue());
                    webSubServicesRegistry.setTopicPayloadKeys(topicPayloadKeys);
                } else {
                    throw new BallerinaConnectorException("Topic Header and/or Payload Keys not specified to dispatch"
                                                                  + " by Topic Header and Payload Key");
                }
            }
            BMap<String, BMap<String, BString>> topicResourceMap =
                    (BMap<String, BMap<String, BString>>) config.get(SERVICE_CONFIG_TOPIC_RESOURCE_MAP);
            if (!(topicResourceMap).isEmpty()) {
                webSubServicesRegistry.setTopicResourceMap(topicResourceMap);
            } else {
                throw new BallerinaConnectorException("Topic-Resource Map not specified to dispatch by "
                                                     + stringTopicIdentifier);
            }
        }
        serviceEndpoint.addNativeData(WEBSUB_SERVICE_REGISTRY, webSubServicesRegistry);

        context.setReturnValues();
    }

}
