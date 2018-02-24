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

    public LocalTransactionInfo(String globalTransactionId, String url, String protocol) {
        this.globalTransactionId = globalTransactionId;
        this.url = url;
        this.protocol = protocol;
        this.transactionLevel = 0;
        this.allowedTransactionRetryCounts = new HashMap<>();
        this.currentTransactionRetryCounts = new HashMap<>();
        this.transactionContextStore = new HashMap<>();
    }

    public void clear() {
        allowedTransactionRetryCounts.clear();
        currentTransactionRetryCounts.clear();
        transactionContextStore.clear();
    }

    public String getGlobalTransactionId() {
        return this.globalTransactionId;
    }

    public String getURL() {
        return this.url;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void beginTransactionBlock(int localTransactionID, int retryCount) {
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

    public int getAllowedRetryCount(int localTransactionID) {
        return allowedTransactionRetryCounts.get(localTransactionID);
    }

    public int getCurrentRetryCount(int localTransactionID) {
        return currentTransactionRetryCounts.get(localTransactionID);
    }

    public BallerinaTransactionContext getTransactionContext(String connectorid) {
        return transactionContextStore.get(connectorid);
    }

    public void registerTransactionContext(String connectorid, BallerinaTransactionContext txContext) {
        transactionContextStore.put(connectorid, txContext);
    }

    public boolean isOuterTransaction() {
        return transactionLevel == 0;
    }

    public void endTransactionBlock() {
        --transactionLevel;
    }
}
