/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.transactions;

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.scheduling.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * {@code TransactionLocalContext} stores the transaction related information.
 *
 * @since 1.0
 */
public class TransactionLocalContext {

    private String globalTransactionId;
    private String url;
    private String protocol;

    private int transactionLevel;
    private Map<String, Integer> allowedTransactionRetryCounts;
    private Map<String, Integer> currentTransactionRetryCounts;
    private Map<String, BallerinaTransactionContext> transactionContextStore;
    private Stack<String> transactionBlockIdStack;
    private Stack<TransactionFailure> transactionFailure;
    private static final TransactionResourceManager transactionResourceManager =
            TransactionResourceManager.getInstance();
    private boolean isResourceParticipant;
    private Object rollbackOnlyError;
    private Object transactionData;
    private BArray transactionId;
    private boolean isTransactional;

    private TransactionLocalContext(String globalTransactionId, String url, String protocol, Object infoRecord) {
        this.globalTransactionId = globalTransactionId;
        this.url = url;
        this.protocol = protocol;
        this.transactionLevel = 0;
        this.allowedTransactionRetryCounts = new HashMap<>();
        this.currentTransactionRetryCounts = new HashMap<>();
        this.transactionContextStore = new HashMap<>();
        this.transactionBlockIdStack = new Stack<>();
        this.transactionFailure = new Stack<>();
        this.rollbackOnlyError = null;
        this.isTransactional = true;
        this.transactionId = ValueCreator.createArrayValue(globalTransactionId.getBytes());
        transactionResourceManager.transactionInfoMap.put(transactionId, infoRecord);
    }

    public static TransactionLocalContext createTransactionParticipantLocalCtx(String globalTransactionId,
            String url, String protocol, Object infoRecord) {
        TransactionLocalContext localContext =
                new TransactionLocalContext(globalTransactionId, url, protocol, infoRecord);
        localContext.setResourceParticipant(true);
        return localContext;
    }

    public static TransactionLocalContext create(String globalTransactionId, String url, String protocol) {
        return new TransactionLocalContext(globalTransactionId, url, protocol, null);
    }

    public String getGlobalTransactionId() {
        return this.globalTransactionId;
    }

    public String getCurrentTransactionBlockId() {
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

    public void beginTransactionBlock(String localTransactionID) {
        transactionBlockIdStack.push(localTransactionID);
        currentTransactionRetryCounts.put(localTransactionID, 0);
        ++transactionLevel;
    }

    public void incrementCurrentRetryCount(String localTransactionID) {
        currentTransactionRetryCounts.putIfAbsent(localTransactionID, 0);
        currentTransactionRetryCounts.computeIfPresent(localTransactionID, (k, v) -> v + 1);
    }

    public BallerinaTransactionContext getTransactionContext(String connectorid) {
        return transactionContextStore.get(connectorid);
    }

    public void registerTransactionContext(String connectorid, BallerinaTransactionContext txContext) {
        transactionContextStore.put(connectorid, txContext);
    }

    /**
     * Is this a retry attempt or initial transaction run.
     *
     * Current retry count = 0 is initial run.
     *
     * @param transactionId transaction block id
     * @return this is a retry runs
     */
    public boolean isRetryAttempt(String transactionId) {
        return  getCurrentRetryCount(transactionId) > 0;
    }

    public boolean isRetryPossible(Strand context, String transactionId) {
        int allowedRetryCount = getAllowedRetryCount(transactionId);
        int currentRetryCount = getCurrentRetryCount(transactionId);
        if (currentRetryCount >= allowedRetryCount) {
            if (currentRetryCount != 0) {
                return false; //Retry count exceeded
            }
        }
        return true;
    }


    public void rollbackTransaction(Strand strand, String transactionBlockId, Object error) {
        transactionContextStore.clear();
        transactionResourceManager.rollbackTransaction(strand, globalTransactionId, transactionBlockId, error);
    }

    public void setRollbackOnlyError(Object error) {
        rollbackOnlyError = error;
    }

    public Object getRollbackOnly() {
        return rollbackOnlyError;
    }

    public void setTransactionData(Object data) {
        transactionData = data;
    }

    public Object getTransactionData() {
        return transactionData;
    }

    public void removeTransactionInfo() {
        transactionResourceManager.transactionInfoMap.remove(transactionId);
    }

    public void notifyLocalParticipantFailure() {
        String blockId = transactionBlockIdStack.peek();
        transactionResourceManager.notifyLocalParticipantFailure(globalTransactionId, blockId);
    }

    public void notifyLocalRemoteParticipantFailure() {
        TransactionResourceManager.getInstance().notifyResourceFailure(globalTransactionId);
    }

    public int getAllowedRetryCount(String localTransactionID) {
        return allowedTransactionRetryCounts.get(localTransactionID);
    }

    private int getCurrentRetryCount(String localTransactionID) {
        return currentTransactionRetryCounts.get(localTransactionID);
    }

    private void resetTransactionInfo() {
        allowedTransactionRetryCounts.clear();
        currentTransactionRetryCounts.clear();
        transactionContextStore.clear();
    }

    public void markFailure() {
        transactionFailure.push(TransactionFailure.at(-1));
    }

    public TransactionFailure getAndClearFailure() {
        if (transactionFailure.empty()) {
            return null;
        }
        TransactionFailure failure = transactionFailure.pop();
        transactionFailure.clear();
        return failure;
    }

    public TransactionFailure getFailure() {
        if (transactionFailure.empty()) {
            return null;
        }
        return transactionFailure.peek();
    }

    public boolean isResourceParticipant() {
        return isResourceParticipant;
    }

    public void setResourceParticipant(boolean resourceParticipant) {
        isResourceParticipant = resourceParticipant;
    }

    public Object getInfoRecord() {
        return transactionResourceManager.transactionInfoMap.get(transactionId);
    }

    public boolean isTransactional() {
        return isTransactional;
    }

    public void setTransactional(boolean transactional) {
        isTransactional = transactional;
    }

    /**
     * Carrier for transaction failure information.
     */
    public static class TransactionFailure {
        private final int offendingIp;

        private TransactionFailure(int offendingIp) {
            this.offendingIp = offendingIp;
        }

        private static TransactionFailure at(int offendingIp) {
            return new TransactionFailure(offendingIp);
        }

        public int getOffendingIp() {
            return offendingIp;
        }
    }

    /**
     * Transaction participant types.
     */
    public enum TransactionParticipantType {
        LOCAL_PARTICIPANT,
        REMOTE_PARTICIPANT,
        NON_PARTICIPANT
    }
}
