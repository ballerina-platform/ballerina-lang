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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.LoggingExceptionListener;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * Connection init function for JMS connection endpoint.
 *
 * @since 0.970
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "createConnection",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.CONNECTION_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class CreateConnection {

    public static void createConnection(Strand strand, ObjectValue connectionObject) {
        MapValue connectionConfig = connectionObject.getMapValue(JmsConstants.CONNECTION_CONFIG);

        Connection connection = JmsUtils.createConnection(connectionConfig);
        try {
            if (connection.getClientID() == null) {
                connection.setClientID(UUID.randomUUID().toString());
            }
            connection.setExceptionListener(new LoggingExceptionListener());
            connection.start();
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error occurred while starting connection.", e);
        }
        connectionObject.addNativeData(JmsConstants.JMS_CONNECTION, connection);
    }

    private CreateConnection() {
    }
}
