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
        setIfNonDaemonThreadRunningAtAllTimes(connectionObject, connectionConfig);
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

    /**
     * Most of the brokers keep a single non-daemon thread running at all times in order to prevent the program from
     * exiting during the JMS asynchronous scenario. This is not always the case for all brokers as it is not in the JMS
     * spec. One broker (among the brokers tested so far) that deviates is the ActiveMQ Artemis broker. This method is
     * to see if a non-daemon thread is always running based on broker kind. We check only for Artemis here because it
     * is the only deviation identified so far.
     *
     * @param connectionObject The Ballerina connection object to set the native data on
     * @param connectionConfig The connection configuration to get the initial context factory
     */
    private static void setIfNonDaemonThreadRunningAtAllTimes(ObjectValue connectionObject, MapValue connectionConfig) {
        connectionObject.addNativeData(JmsConstants.NON_DAEMON_THREAD_RUNNING, !connectionConfig.getStringValue(
                JmsConstants.ALIAS_INITIAL_CONTEXT_FACTORY).contains(JmsConstants.ARTEMIS_ICF));
    }

    private CreateConnection() {
    }
}
