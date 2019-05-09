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

package org.ballerinalang.messaging.artemis;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.transactions.BallerinaTransactionContext;
import org.ballerinalang.util.transactions.TransactionLocalContext;
import org.ballerinalang.util.transactions.TransactionResourceManager;

import java.util.Objects;
import java.util.UUID;

import javax.transaction.xa.XAResource;

/**
 * Session wrapper class with Ballerina transaction logic.
 */
public class ArtemisTransactionContext implements BallerinaTransactionContext {
    private final String connectorId;
    private ClientSession session;
    private BMap<String, BValue> sessionObj;

    public ArtemisTransactionContext(BMap<String, BValue> sessionObj) {
        this.sessionObj = sessionObj;
        session = (ClientSession) sessionObj.getNativeData(ArtemisConstants.ARTEMIS_SESSION);
        connectorId = UUID.randomUUID().toString();
    }

    @Override
    public void commit() {
        try {
            session.commit();
        } catch (ActiveMQException e) {
            throw new BallerinaException("Transaction commit failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void rollback() {
        try {
            session.rollback();
        } catch (ActiveMQException e) {
            throw new BallerinaException("Transaction rollback failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public XAResource getXAResource() {
        // XA is not supported at the moment
        return null;
    }

    public void handleTransactionBlock(Context context, String objectType) {
        if (ArtemisUtils.isAnonymousSession(sessionObj)) {
            if (!context.isInTransaction()) {
                try {
                    session.commit();
                    return;
                } catch (ActiveMQException e) {
                    context.setError(ArtemisUtils.getError(context, e));
                }
            }
        } else {
            if (!context.isInTransaction()) {
                context.setError(ArtemisUtils.getError(context, "The Session used by the Artemis " + objectType +
                        " object is transacted. Hence " + objectType +
                        " transacted actions cannot be used outside a transaction block"));
            }
        }
        TransactionLocalContext transactionLocalContext = context.getLocalTransactionInfo();
        BallerinaTransactionContext txContext = transactionLocalContext.getTransactionContext(connectorId);
        if (Objects.isNull(txContext)) {
            transactionLocalContext.registerTransactionContext(connectorId, this);
            String globalTxId = transactionLocalContext.getGlobalTransactionId();
            String currentTxBlockId = transactionLocalContext.getCurrentTransactionBlockId();
            TransactionResourceManager.getInstance().register(globalTxId, currentTxBlockId, this);
        }
    }

}
