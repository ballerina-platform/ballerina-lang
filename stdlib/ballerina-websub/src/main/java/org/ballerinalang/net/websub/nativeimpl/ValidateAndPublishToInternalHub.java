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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.hub.Hub;

/**
 * Native function to validate that the hub URL passed indicates the underlying Ballerina Hub (if started) and publish
 * against a topic in the default Ballerina Hub's underlying broker.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "validateAndPublishToInternalHub",
        args = {@Argument(name = "hubUrl", type = TypeKind.STRING),
                @Argument(name = "topic", type = TypeKind.STRING),
                @Argument(name = "payload", type = TypeKind.JSON)},
        returnType = {@ReturnType(type = TypeKind.STRUCT)}
)
public class ValidateAndPublishToInternalHub extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String hubUrl = context.getStringArgument(0);
        String topic = context.getStringArgument(1);
        BJSON jsonPayload = (BJSON) context.getRefArgument(0);
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted() && hubInstance.retrieveHubUrl().equals(hubUrl)) {
            String payload = jsonPayload.stringValue();
            try {
                Hub.getInstance().publish(topic, payload);
                context.setReturnValues();
            } catch (BallerinaWebSubException e) {
                context.setReturnValues(BLangVMErrors.createError(context, e.getMessage()));
            }
        } else {
            context.setReturnValues(BLangVMErrors.createError(context,
                                          "Internal Ballerina Hub not initialized or incorrectly referenced"));
        }
    }

}
