/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.queue.receiver;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.CloseConsumerHandler;

/**
 * Close the message consumer object.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "closeQueueReceiver",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "QueueReceiver", structPackage = "ballerina.jms"),
        args = {
                @Argument(name = "connector", type = TypeKind.STRUCT, structType = "QueueReceiverConnector")
        },
        isPublic = true
)
public class CloseReceiver implements NativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        CloseConsumerHandler.handle(context);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
