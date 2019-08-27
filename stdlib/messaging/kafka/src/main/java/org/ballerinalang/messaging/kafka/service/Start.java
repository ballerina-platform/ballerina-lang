/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.messaging.kafka.service;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.exceptions.KafkaConnectorException;
import org.ballerinalang.messaging.kafka.impl.KafkaServerConnectorImpl;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.PrintStream;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ORG_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SERVER_CONNECTOR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SERVICE_STARTED;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getBrokerNames;

/**
 * Start server connector.
 */

@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "start",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)
public class Start {
    private static final PrintStream console = System.out;

    public static Object start(Strand strand, ObjectValue listener) {
        KafkaServerConnectorImpl serverConnector = (KafkaServerConnectorImpl) listener.getNativeData(SERVER_CONNECTOR);
        try {
            serverConnector.start();
            console.println(SERVICE_STARTED + getBrokerNames(listener));
        } catch (KafkaConnectorException e) {
            return KafkaUtils.createKafkaError(e.getMessage(), CONSUMER_ERROR);
        }
        return null;
    }
}
