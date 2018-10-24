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
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.hub.Hub;
import org.ballerinalang.net.websub.hub.HubSubscriber;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.util.List;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_CREATED_AT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * Extern function to retrieve details of subscribers registered to receive updates for a particular topic.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "getSubscribers",
        args = {@Argument(name = "topic", type = TypeKind.STRING)},
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WebSubHub", structPackage = WEBSUB_PACKAGE),
        returnType = @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.RECORD,
                                 structType = SUBSCRIPTION_DETAILS, structPackage = WEBSUB_PACKAGE),
        isPublic = true
)
public class GetSubscribers extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String topic = context.getStringArgument(0);
        List<HubSubscriber> subscribers = Hub.getInstance().getSubscribers();

        final PackageInfo packageInfo = context.getProgramFile().getPackageInfo(WEBSUB_PACKAGE);
        final StructureTypeInfo structInfo = packageInfo.getStructInfo(SUBSCRIPTION_DETAILS);
        BRefValueArray subscriberDetailArray = new BRefValueArray(structInfo.getType());

        for (HubSubscriber subscriber : subscribers) {
            if (subscriber.getTopic().equals(topic)) {
                BMap<String, BValue> subscriberDetail = BLangVMStructs.createBStruct(
                        structInfo, subscriber.getCallback(),
                        subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_LEASE_SECONDS),
                        subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_CREATED_AT));
                subscriberDetailArray.append(subscriberDetail);
            }
        }
        context.setReturnValues(subscriberDetailArray);
    }
}
