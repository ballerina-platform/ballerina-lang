/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.HashMap;
import java.util.Map;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * {@code BallerinaTransactionManager} manages local and distributed transactions in ballerina.
 *
 * @since 0.8.7
 */
public class BallerinaTransactionManager {
    private Map<String, BallerinaTransactionContext> transactionContextStore;
    private TransactionManager transactionManager;
    private int transactionLevel; //level of the nested transaction
    private boolean transactionError; //status of nested transactions
    private Map<Integer, Integer> allowedTransactionRetryCounts;
    private Map<Integer, Integer> currentTransactionRetryCounts;

    public BallerinaTransactionManager() {
        this.transactionContextStore = new HashMap<>();
        this.transactionLevel = 0;
        this.transactionError = false;
        this.allowedTransactionRetryCounts = new HashMap<>();
        this.currentTransactionRetryCounts = new HashMap<>();
    }

    public void registerTransactionContext(String id, BallerinaTransactionContext txContext) {
        transactionContextStore.put(id, txContext);
    }

    public BallerinaTransactionContext getTransactionContext(String id) {
        return transactionContextStore.get(id);
    }

    public void setTransactionError(boolean transactionError) {
        this.transactionError = transactionError;
    }

    public void beginTransactionBlock(int transactionID, int retryCount) {
        allowedTransactionRetryCounts.put(transactionID, retryCount);
        currentTransactionRetryCounts.put(transactionID, 0);
        ++transactionLevel;
    }

    public int getAllowedRetryCount(int transactionId) {
        return allowedTransactionRetryCounts.get(transactionId);
    }

    public int getCurrentRetryCount(int transactionId) {
        return currentTransactionRetryCounts.get(transactionId);
    }

    public void incrementCurrentRetryCount(int transactionId) {
        int count = currentTransactionRetryCounts.containsKey(transactionId) ?
                currentTransactionRetryCounts.get(transactionId) :
                0;
        currentTransactionRetryCounts.put(transactionId, count + 1);
    }

    public void endTransactionBlock() {
        --transactionLevel;
        if (transactionLevel == 0) {
            closeAllConnections();
        }
    }

    public void commitTransactionBlock() {
        if (transactionLevel == 1 && !this.transactionError) {
            commitNonXAConnections();
            closeAllConnections();
            commitXATransaction();
        }
    }

    public void rollbackTransactionBlock() {
        if (transactionLevel == 1) {
            rollbackNonXAConnections();
            rollbackXATransaction();
        }
    }

    public boolean isOuterTransaction() {
        return transactionLevel == 0;
    }

    public void setXATransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public boolean hasXATransactionManager() {
        return this.transactionManager != null;
    }

    public Transaction getXATransaction() {
        Transaction tx = null;
        try {
            tx = transactionManager.getTransaction();
        } catch (Exception e) {
            throw new BallerinaException("xa transaction not found");
        }
        return tx;
    }

    public boolean isInXATransaction() {
        if (transactionManager == null) {
            return false;
        }
        try {
            return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (Exception e) {
            return false;
        }
    }

    public void beginXATransaction() {
        if (transactionManager == null) {
            return;
        }
        try {
            transactionManager.begin();
        } catch (Exception e) {
            throw new BallerinaException("begin xa transaction failed");
        }
    }

    private void commitXATransaction() {
        if (transactionManager == null) {
            return;
        }
        try {
            if (isInXATransaction()) {
                transactionManager.commit();
            }
        } catch (Exception e) {
            throw new BallerinaException("commit xa transaction failed");
        }
    }

    private void rollbackXATransaction() {
        if (transactionManager == null) {
            return;
        }
        try {
            if (isInXATransaction()) {
                transactionManager.rollback();
            }
        } catch (Exception e) {
            throw new BallerinaException("rollback xa transaction failed");
        }
    }

    private void commitNonXAConnections() {
        transactionContextStore.forEach((k, v) -> {
            if (!v.isXAConnection()) {
                v.commit();
            }
        });
    }

    private void rollbackNonXAConnections() {
        transactionContextStore.forEach((k, v) -> {
            if (!v.isXAConnection()) {
                v.rollback();
            }
        });
    }

    private void closeAllConnections() {
        transactionContextStore.forEach((k, v) -> {
            v.close();
        });
    }
}
