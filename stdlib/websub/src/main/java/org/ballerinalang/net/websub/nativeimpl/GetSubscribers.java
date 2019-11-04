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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.hub.Hub;
import org.ballerinalang.net.websub.hub.HubSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_CREATED_AT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_ID;

/**
 * Extern function to retrieve details of subscribers registered to receive updates for a particular topic.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "getSubscribers",
        args = {@Argument(name = "topic", type = TypeKind.STRING)},
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Hub", structPackage = WEBSUB_PACKAGE),
        returnType = @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.RECORD,
                                 structType = SUBSCRIPTION_DETAILS, structPackage = WEBSUB_PACKAGE),
        isPublic = true
)
public class GetSubscribers {

    private static final Logger log = LoggerFactory.getLogger(GetSubscribers.class);

    public static ArrayValue getSubscribers(Strand strand, ObjectValue webSubHub, String topic) {
        ArrayValue subscriberDetailArray = null;
        try {
            List<HubSubscriber> subscribers = Hub.getInstance().getSubscribers();
            MapValue<String, Object> subscriberDetailsRecordValue = BallerinaValues.createRecordValue(WEBSUB_PACKAGE_ID,
                                                                                     SUBSCRIPTION_DETAILS);
            subscriberDetailArray = new ArrayValue(new BArrayType(subscriberDetailsRecordValue.getType()));
            for (HubSubscriber subscriber : subscribers) {
                if (topic.equals(subscriber.getTopic())) {
                    MapValue<String, Object> subscriberDetail = BallerinaValues.createRecord(
                            subscriberDetailsRecordValue, subscriber.getCallback(),
                            subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_LEASE_SECONDS),
                            subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_CREATED_AT));
                    subscriberDetailArray.append(subscriberDetail);
                }
            }
        } catch (Exception ex) {
            log.error("Error occurred while getting available subscribers.", ex);
        }
        return subscriberDetailArray;
    }
}
