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

package org.ballerinalang.net.jms.nativeimpl.endpoint.connection;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.LoggingExceptionListener;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * Connection init function for JMS connection endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "createConnection",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection", structPackage = "ballerina.jms"),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ConnectionConfiguration")
        },
        isPublic = true
)
public class CreateConnection implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct connectionBObject = BallerinaAdapter.getReceiverStruct(context);
        Struct connectionConfig = connectionBObject.getStructField(Constants.CONNECTION_CONFIG);

        Connection connection = JMSUtils.createConnection(connectionConfig);
        try {
            connection.setExceptionListener(new LoggingExceptionListener());
            connection.start();
        } catch (JMSException e) {
            JMSUtils.throwBallerinaException("Error occurred while starting connection.", context, e);
        }
        connectionBObject.addNativeData(Constants.JMS_CONNECTION, connection);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }


}
