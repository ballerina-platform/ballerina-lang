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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.websub.hub.Hub;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_CALLBACK;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_TOPIC;

/**
 * Extern function to add a subscription to the default Ballerina Hub's underlying broker.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "addSubscription",
        args = {@Argument(name = "subscriptionDetails", type = TypeKind.RECORD)},
        isPublic = true
)
public class AddSubscription extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static void addSubscription(Strand strand, MapValue<String, Object> subscriptionDetails) {
        String topic = subscriptionDetails.getStringValue(SUBSCRIPTION_DETAILS_TOPIC);
        String callback = subscriptionDetails.getStringValue(SUBSCRIPTION_DETAILS_CALLBACK);
        Hub.getInstance().registerSubscription(strand, topic, callback, subscriptionDetails);
    }
}
