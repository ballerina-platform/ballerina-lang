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

package org.ballerinalang.messaging.rabbitmq;

import com.rabbitmq.client.Channel;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.util.transactions.BallerinaTransactionContext;

import java.io.IOException;

import javax.transaction.xa.XAResource;

/**
 * Channel wrapper class to handle Ballerina transaction logic.
 *
 * @since 0.995
 */
public class RabbitMQTransactionContext implements BallerinaTransactionContext {
    private Channel channel;

    /**
     * Initializes the rabbitmq transaction context.
     *
     * @param channelObject RabbitMQ Channel object.
     * @param connectorId Connector ID.
     */
    public RabbitMQTransactionContext(ObjectValue channelObject, String connectorId) {
        this.channel = (Channel) channelObject.getNativeData(RabbitMQConstants.CHANNEL_NATIVE_OBJECT);
    }

    @Override
    public void commit() {
        try {
            channel.txCommit();
        } catch (IOException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.COMMIT_FAILED
                    + exception.getMessage(), exception);
        }
    }

    @Override
    public void rollback() {
        try {
            channel.txRollback();
        } catch (IOException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.ROLLBACK_FAILED
                    + exception.getMessage(), exception);
        }

    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public XAResource getXAResource() {
        // Not supported
        return null;
    }

    /**
     * Handles the transaction block if the context is in transaction.
     */
    public void handleTransactionBlock() {
     // TODO: Transaction handling logic
    }
}
