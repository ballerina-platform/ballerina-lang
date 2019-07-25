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

package org.ballerinalang.net.jms.nativeimpl.endpoint.common;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.BallerinaTransactionContext;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.transactions.TransactionResourceManager;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import java.util.Objects;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;

/**
 * Session wrapper class with Ballerina transaction logic.
 */
public class SessionConnector implements BallerinaTransactionContext {
    private final String connectorId;
    private Session session;

    public SessionConnector(Session session) {
        this.session = session;
        connectorId = UUID.randomUUID().toString();
    }

    /**
     * Getter for connectorId.
     */
    private String getConnectorId() {
        return connectorId;
    }

    /**
     * Getter for session.
     *
     * @return connector session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public void commit() {
        try {
            if (session.getAcknowledgeMode() == Session.SESSION_TRANSACTED) {
                session.commit();
            }
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("transaction commit failed: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void rollback() {
        try {
            if (session.getAcknowledgeMode() == Session.SESSION_TRANSACTED) {
                session.rollback();
            }
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("transaction rollback failed: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public XAResource getXAResource() {
        if (session instanceof XASession) {
            return ((XASession) session).getXAResource();
        } else {
            return null;
        }
    }

    public void handleTransactionBlock(Strand strand) throws JMSException {
        if (getSession().getAcknowledgeMode() == Session.SESSION_TRANSACTED) {
            // if the action is not called inside a transaction block
            if (!strand.isInTransaction()) {
                BallerinaAdapter.throwBallerinaException(
                        "jms transacted session objects should be used inside a transaction block ");
            }

            TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
            BallerinaTransactionContext txContext = transactionLocalContext.getTransactionContext(
                    getConnectorId());
            if (Objects.isNull(txContext)) {
                transactionLocalContext.registerTransactionContext(getConnectorId(), this);
                String globalTxId = transactionLocalContext.getGlobalTransactionId();
                String currentTxBlockId = transactionLocalContext.getCurrentTransactionBlockId();
                TransactionResourceManager.getInstance().register(globalTxId, currentTxBlockId, this);
            }
        }
    }
}
