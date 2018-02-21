/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.XAException;

/**
 * {@code TransactionResourceManager} registry for transaction contexts.
 *
 * @since 0.964.0
 */
public class TransactionResourceManager {

    private static TransactionResourceManager transactionResourceManager = null;
    private static final Logger log = LoggerFactory.getLogger(TransactionResourceManager.class);
    private Map<String, List<BallerinaTransactionContext>> resourceRegistry;

    private TransactionResourceManager() {
        resourceRegistry = new HashMap<>();
    }

    public static TransactionResourceManager getInstance() {
        if (transactionResourceManager == null) {
            synchronized (TransactionResourceManager.class) {
                if (transactionResourceManager == null) {
                    transactionResourceManager = new TransactionResourceManager();
                }
            }
        }
        return transactionResourceManager;
    }

    public void register(String transactionId, BallerinaTransactionContext txContext) {
        resourceRegistry.computeIfAbsent(transactionId, resourceList -> new ArrayList<>()).add(txContext);
    }

    public boolean prepare(String transactionId) {
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        for (BallerinaTransactionContext ctx : txContextList) {
            try {
                ctx.getXAResource().prepare(null); //TODO:Pass valid xid
            } catch (XAException e) {
                log.error("error in prepare the transaction, " + transactionId + ":" + e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    public boolean notifyCommit(String transactionId) {
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        for (BallerinaTransactionContext ctx : txContextList) {
            try {
                ctx.getXAResource().commit(null, false); //TODO:Pass valid xid and phase
            } catch (XAException e) {
                log.error("error in commit the transaction, " + transactionId + ":" + e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    public boolean notifyAbort(String transactionId) {
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        for (BallerinaTransactionContext ctx : txContextList) {
            try {
                ctx.getXAResource().rollback(null); //TODO:Pass valid xid
            } catch (XAException e) {
                log.error("error in abort the transaction, " + transactionId + ":" + e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
}
