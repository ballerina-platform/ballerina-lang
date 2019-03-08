/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.connection;

import com.rabbitmq.client.Connection;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.messaging.rabbitmq.util.ConnectionUtils;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CONNECTION_STRUCT;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.ORG_NAME;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.STRUCT_PACKAGE_RABBITMQ;
import static org.ballerinalang.messaging.rabbitmq.RabbitMQConstants.CONNECTION_NATIVE_OBJECT;

/**
 * Native function for initializing a RabbitMQ Connection.
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PACKAGE_RABBITMQ,
        functionName = "createConnection",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = CONNECTION_STRUCT,
                structPackage = STRUCT_PACKAGE_RABBITMQ),
        isPublic = true
)
public class CreateConnection implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        BMap<String, BValue> connectionBObject = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> connectionConfig = (BMap<String, BValue>) context.getRefArgument(1);
        Connection connection = ConnectionUtils.createConnection(connectionConfig);
        connectionBObject.addNativeData(CONNECTION_NATIVE_OBJECT, connection);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
