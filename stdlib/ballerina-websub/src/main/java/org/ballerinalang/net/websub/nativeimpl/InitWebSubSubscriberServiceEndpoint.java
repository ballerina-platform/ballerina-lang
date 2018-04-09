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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.net.http.serviceendpoint.AbstractHttpNativeFunction;
import org.ballerinalang.net.websub.WebSubServicesRegistry;
import org.ballerinalang.net.websub.WebSubSubscriberConstants;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Initialize the WebSub subscriber endpoint.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "initWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "SubscriberServiceEndpoint",
                structPackage = WebSubSubscriberConstants.WEBSUB_PACKAGE_PATH)
)
public class InitWebSubSubscriberServiceEndpoint extends AbstractHttpNativeFunction {

    @Override
    public void execute(Context context) {

        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = ConnectorSPIModelHelper.createStruct(
                (BStruct) ((BStruct) (subscriberServiceEndpoint.getVMValue())).getRefField(1));
        BStruct config = (BStruct) ((BStruct) context.getRefArgument(0)).getRefField(0);
        WebSubServicesRegistry webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry());
        BString topicIdentifier = (BString) config.getRefField(1);
        if (topicIdentifier != null) {
            String stringTopicIdentifier = topicIdentifier.stringValue();
            webSubServicesRegistry.setTopicIdentifier(stringTopicIdentifier);
            if (WebSubSubscriberConstants.TOPIC_ID_HEADER.equals(stringTopicIdentifier)) {
                BString topicHeader = (BString) config.getRefField(2);
                if (topicHeader != null) {
                    webSubServicesRegistry.setTopicHeader(topicHeader.stringValue());
                } else {
                    throw new BallerinaException("Topic Header not specified to dispatch by Header");
                }
            } else if (WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY.equals(stringTopicIdentifier)) {
                BString topicPayloadKey = (BString) config.getRefField(3);
                if (topicPayloadKey != null) {
                    webSubServicesRegistry.setTopicPayloadKey(topicPayloadKey.stringValue());
                } else {
                    throw new BallerinaException("Payload Key not specified to dispatch by Payload Key");
                }
            } else {
                BString topicHeader = (BString) config.getRefField(2);
                BString topicPayloadKey = (BString) config.getRefField(3);
                if (topicHeader != null && topicPayloadKey != null) {
                    webSubServicesRegistry.setTopicHeader(topicHeader.stringValue());
                    webSubServicesRegistry.setTopicPayloadKey(topicPayloadKey.stringValue());
                } else {
                    throw new BallerinaException("Topic Header and/or Payload Key not specified to dispatch by Topic"
                                                         + " Header and Payload Key");
                }
            }
            if (!((BMap<String, BString>) config.getRefField(4)).isEmpty()) {
                webSubServicesRegistry.setTopicResourceMap((BMap<String, BString>) config.getRefField(4));
            } else {
                throw new BallerinaException("Topic-Resource Map not specified to dispatch by "
                                                     + stringTopicIdentifier);
            }
        }
        serviceEndpoint.addNativeData(WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY, webSubServicesRegistry);

        context.setReturnValues();
    }

}
