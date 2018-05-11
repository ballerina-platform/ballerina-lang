/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.queue.actions;

import org.ballerinalang.model.NativeCallableUnit;


/**
 * {@code AbstractJMSAction} is the base class for all JMS Connector Actions.
 */

public abstract class AbstractJMSAction implements NativeCallableUnit {

//    /**
//     * Get tx SessionWrapper.
//     * If the transaction context is already started, we can re-use the session, otherwise acquire a new SessionWrapper
//     * from the transport.
//     *
//     * @param context Ballerina context.
//     * @param jmsClientConnector transport level JMSClientConnector of this Ballerina Connector.
//     * @param connectorKey Id of the Ballerina Connector.
//     * @return
//     * @throws JMSConnectorException Error when acquiring the session.
//     */
//    protected SessionWrapper getTxSession(Context context, JMSClientConnector jmsClientConnector, String connectorKey)
//            throws JMSConnectorException {
//        SessionWrapper sessionWrapper;
//
//        LocalTransactionInfo localTransactionInfo = context.getLocalTransactionInfo();
//
//        BallerinaTransactionContext txContext = localTransactionInfo.getTransactionContext(connectorKey);
//        // if transaction initialization has not yet been done
//        // (if this is the first transacted action happens from this particular connector within this
//        // transaction block)
//        if (txContext == null) {
//            sessionWrapper = jmsClientConnector.acquireSession();
//            txContext = new JMSTransactionContext(sessionWrapper, jmsClientConnector);
//            //Handle XA initialization
//            if (txContext.getXAResource() != null) {
//                TransactionResourceManager.getInstance()
//                                          .beginXATransaction(localTransactionInfo.getGlobalTransactionId(),
//                                                              localTransactionInfo.getCurrentTransactionBlockId(),
//                                                              txContext.getXAResource());
//            }
//            localTransactionInfo.registerTransactionContext(connectorKey, txContext);
//            TransactionResourceManager.getInstance().register(localTransactionInfo.getGlobalTransactionId(),
//                                                              localTransactionInfo.getCurrentTransactionBlockId(),
//                                                              txContext);
//        } else {
//            sessionWrapper = ((JMSTransactionContext) txContext).getSessionWrapper();
//        }
//        return sessionWrapper;
//    }
}
