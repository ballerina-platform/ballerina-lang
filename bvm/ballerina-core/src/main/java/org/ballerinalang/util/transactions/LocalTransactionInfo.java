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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * {@code LocalTransactionInfo} stores the transaction related information.
 *
 * @since 0.964.0
 */
public class LocalTransactionInfo {

    private String globalTransactionId;
    private String url;
    private String protocol;

    private int transactionLevel;
    private Map<Integer, Integer> allowedTransactionRetryCounts;
    private Map<Integer, Integer> currentTransactionRetryCounts;
    private Map<String, BallerinaTransactionContext> transactionContextStore;
    private Stack<Integer> transactionBlockIdStack;

    public LocalTransactionInfo(String globalTransactionId, String url, String protocol) {
        this.globalTransactionId = globalTransactionId;
        this.url = url;
        this.protocol = protocol;
        this.transactionLevel = 0;
        this.allowedTransactionRetryCounts = new HashMap<>();
        this.currentTransactionRetryCounts = new HashMap<>();
        this.transactionContextStore = new HashMap<>();
        transactionBlockIdStack = new Stack<>();
    }

    public String getGlobalTransactionId() {
        return this.globalTransactionId;
    }

    public int getCurrentTransactionBlockId() {
        return transactionBlockIdStack.peek();
    }

    public boolean hasTransactionBlock() {
        return !transactionBlockIdStack.empty();
    }

    public String getURL() {
        return this.url;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void beginTransactionBlock(int localTransactionID, int retryCount) {
        transactionBlockIdStack.push(localTransactionID);
        allowedTransactionRetryCounts.put(localTransactionID, retryCount);
        currentTransactionRetryCounts.put(localTransactionID, 0);
        ++transactionLevel;
    }

    public void incrementCurrentRetryCount(int localTransactionID) {
        int count = currentTransactionRetryCounts.containsKey(localTransactionID) ?
                currentTransactionRetryCounts.get(localTransactionID) :
                0;
        currentTransactionRetryCounts.put(localTransactionID, count + 1);
    }

    public BallerinaTransactionContext getTransactionContext(String connectorid) {
        return transactionContextStore.get(connectorid);
    }

    public void registerTransactionContext(String connectorid, BallerinaTransactionContext txContext) {
        transactionContextStore.put(connectorid, txContext);
    }

    public boolean isRetryPossible(int transactionId) {
        boolean retryPossible = true;
        int allowedRetryCount = getAllowedRetryCount(transactionId);
        int currentRetryCount = getCurrentRetryCount(transactionId);
        if (currentRetryCount >= allowedRetryCount) {
            if (currentRetryCount != 0) {
                retryPossible = false;
            }
        }
        return retryPossible;
    }

    public boolean onTransactionFailed(int transactionBlockId) {
        boolean bNotifyCoordinator = false;
        if (transactionLevel == 1) {
            int currentCount = getCurrentRetryCount(transactionBlockId);
            int allowedCount = getAllowedRetryCount(transactionBlockId);
            //local retry is attempted without notifying the coordinator. If all the attempts are failed, notify
            //the coordinator with transaction abort.
            if (allowedCount == 0 || currentCount == allowedCount) {
                bNotifyCoordinator = true;
            } else {
                transactionContextStore.clear();
                TransactionResourceManager.getInstance().rollbackTransaction(globalTransactionId, transactionBlockId);
            }
        }
        return bNotifyCoordinator;
    }

    public boolean onTransactionAbort() {
        return (transactionLevel == 1);
    }

    public boolean onTransactionEnd(int transactionBlockId) {
        boolean bNotifyCoordinator = false;
        transactionBlockIdStack.pop();
        --transactionLevel;
        if (transactionLevel == 0) {
            TransactionResourceManager.getInstance().endXATransaction(globalTransactionId, transactionBlockId);
            resetTransactionInfo();
            bNotifyCoordinator = true;
        }
        return bNotifyCoordinator;

    }

    private int getAllowedRetryCount(int localTransactionID) {
        return allowedTransactionRetryCounts.get(localTransactionID);
    }

    private int getCurrentRetryCount(int localTransactionID) {
        return currentTransactionRetryCounts.get(localTransactionID);
    }

    private void resetTransactionInfo() {
        allowedTransactionRetryCounts.clear();
        currentTransactionRetryCounts.clear();
        transactionContextStore.clear();
    }
}
