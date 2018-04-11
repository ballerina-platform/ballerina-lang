/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Receive} is the poll action implementation of the JMS Client Connector.
 *
 * @since 0.95.5
 */
@BallerinaFunction(orgName = "ballerina", packageName = "queue",
                   functionName = "receiveWithSelector",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "ClientConnector",
                                        structPackage =
                                                "ballerina.queue"),
                   args = {
                           @Argument(name = "client",
                                     type = TypeKind.STRUCT),
                           @Argument(name = "queueName",
                                     type = TypeKind.STRING),
                           @Argument(name = "timeout",
                                     type = TypeKind.INT),
                           @Argument(name = "selector",
                                     type = TypeKind.STRING)
                   },
                   returnType = {
                           @ReturnType(type = TypeKind.STRUCT,
                                       structPackage = "ballerina.queue",
                                       structType = "Message")
                   }
)
public class SelectorReceive extends Receive {
    private static final Logger log = LoggerFactory.getLogger(SelectorReceive.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        String messageSelector = context.getStringArgument(1);

        startSubscriber(context, callableUnitCallback, messageSelector);
    }
}
