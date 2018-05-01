/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.queue.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.InterruptibleNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.queue.BrokerUtils;
import org.ballerinalang.queue.WorkflowSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BallerinaFunction(orgName = "ballerina", packageName = "queue",
                   functionName = "receive",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "ClientConnector",
                                        structPackage =
                                                "ballerina.queue"),
                   args = {
                           @Argument(name = "client",
                                     type = TypeKind.STRUCT),
                           @Argument(name = "map",
                                     type = TypeKind.JSON),
                   },
                   returnType = {
                           @ReturnType(type = TypeKind.STRUCT,
                                       structPackage = "ballerina.queue",
                                       structType = "Message")
                   }
)
public class Receive implements InterruptibleNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Receive.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        BValue jsonMap = context.getRefArgument(1);
        String map = ((BJSON) jsonMap).getMessageAsString();
        String selector = "correlationId = '"+ map + "'";
        startSubscriber(context, callableUnitCallback, selector);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean persistBeforeOperation() {
        return true;
    }

    @Override
    public boolean persistAfterOperation() {
        return true;
    }

    protected void startSubscriber(Context context, CallableUnitCallback callableUnitCallback, String selectorMessage){
        // Extract argument values
        String destination = BLangConnectorSPIUtil.getConnectorEndpointStruct(context).getStructField("config")
                .getStringField("destination");
        log.info("Starting subscription on the destination - " + destination);
        if (selectorMessage == null) {
            BrokerUtils.addSubscription(destination, new WorkflowSubscriber(destination, callableUnitCallback, context));
        } else {
            BrokerUtils.addSubscription(destination, new WorkflowSubscriber(destination, callableUnitCallback,
                    context), selectorMessage);
        }

    }
}