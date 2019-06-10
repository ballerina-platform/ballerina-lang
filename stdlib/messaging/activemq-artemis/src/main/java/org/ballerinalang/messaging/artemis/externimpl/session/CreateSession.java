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
 *
 */

package org.ballerinalang.messaging.artemis.externimpl.session;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisTransactionContext;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function for Artemis session creation.
 *
 * @since 0.995
 */

@BallerinaFunction(
        orgName = ArtemisConstants.BALLERINA,
        packageName = ArtemisConstants.ARTEMIS,
        functionName = "createSession",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = ArtemisConstants.SESSION_OBJ,
                structPackage = ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS
        ),
        args = {
                @Argument(
                        name = "con",
                        type = TypeKind.OBJECT,
                        structType = ArtemisConstants.CONNECTION_OBJ
                ),
                @Argument(
                        name = "config",
                        type = TypeKind.RECORD,
                        structType = "SessionConfiguration"
                )
        }
)
public class CreateSession extends BlockingNativeCallableUnit {
    private static final Logger logger = LoggerFactory.getLogger(CreateSession.class);

    @Override
    public void execute(Context context) {
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> sessionObj = (BMap<String, BValue>) context.getRefArgument(0);
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> connection = (BMap<String, BValue>) context.getRefArgument(1);
        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        BMap<String, BValue> config = (BMap<String, BValue>) context.getRefArgument(2);

        ServerLocator serverLocator = (ServerLocator) connection.getNativeData(
                ArtemisConstants.ARTEMIS_CONNECTION_POOL);
        ClientSessionFactory sessionFactory =
                (ClientSessionFactory) connection.getNativeData(ArtemisConstants.ARTEMIS_SESSION_FACTORY);
        try {
            String username = null;
            String password = null;
            BValue userValue = config.get(ArtemisConstants.USERNAME);
            if (userValue instanceof BString) {
                username = userValue.stringValue();
            }
            BValue passValue = config.get(ArtemisConstants.PASSWORD);
            if (passValue instanceof BString) {
                password = passValue.stringValue();
            }
            boolean autoCommitSends = ((BBoolean) config.get(ArtemisConstants.AUTO_COMMIT_SENDS)).booleanValue();
            boolean autoCommitAcks = ((BBoolean) config.get(ArtemisConstants.AUTO_COMMIT_ACKS)).booleanValue();
            ClientSession session = sessionFactory.createSession(username, password, false, autoCommitSends,
                                                                 autoCommitAcks, serverLocator.isPreAcknowledge(),
                                                                 serverLocator.getAckBatchSize());
            sessionObj.addNativeData(ArtemisConstants.ARTEMIS_SESSION, session);
            if (!autoCommitSends || !autoCommitAcks) {
                sessionObj.addNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT,
                                         new ArtemisTransactionContext(sessionObj));
            }
        } catch (ActiveMQException e) {
            ArtemisUtils.throwException("Error occurred while starting session", context, e, logger);
        }
    }
}
