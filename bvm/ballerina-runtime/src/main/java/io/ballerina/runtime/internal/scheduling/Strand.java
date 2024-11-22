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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.internal.utils.ErrorUtils;
import io.ballerina.runtime.transactions.TransactionLocalContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.CURRENT_TRANSACTION_CONTEXT_PROPERTY;

/**
 * Strand base class used with jvm code generation for functions.
 *
 * @since 0.955.0
 */
public class Strand {

    private final int id;
    private static final AtomicInteger nextStrandId = new AtomicInteger(0);
    private Map<String, Object> globalProps;

    public final String name;
    public final boolean isIsolated;
    public boolean cancelled;
    public Scheduler scheduler;
    public TransactionLocalContext currentTrxContext;
    public Stack<TransactionLocalContext> trxContexts;
    public WorkerChannelMap workerChannelMap;
    public int acquiredLockCount;

    public Strand(Scheduler scheduler, String strandName, Strand parent, boolean isIsolated,
                  Map<String, Object> properties, WorkerChannelMap workerChannelMap) {
        this.id = nextStrandId.incrementAndGet();
        this.name = Objects.requireNonNullElse(strandName, "$anon");
        this.scheduler = scheduler;
        this.trxContexts = new Stack<>();
        this.isIsolated = isIsolated;
        if (properties != null) {
            this.globalProps = properties;
        } else if (parent != null && parent.globalProps != null) {
            this.globalProps = new HashMap<>(parent.globalProps);
        } else {
            this.globalProps = new HashMap<>();
        }
        this.workerChannelMap = workerChannelMap;
    }

    public Strand(Scheduler scheduler, String strandName, Strand parent, boolean isIsolated,
                  Map<String, Object> properties, WorkerChannelMap workerChannelMap,
                  TransactionLocalContext currentTrxContext) {
        this(scheduler, strandName, parent, isIsolated, properties, workerChannelMap);
        if (currentTrxContext != null) {
            this.trxContexts = parent.trxContexts;
            this.trxContexts.push(currentTrxContext);
            this.currentTrxContext = currentTrxContext;
        } else {
            Object currentContext = this.getProperty(CURRENT_TRANSACTION_CONTEXT_PROPERTY);
            if (currentContext != null) {
                TransactionLocalContext branchedContext =
                        createTrxContextBranch((TransactionLocalContext) currentContext, this.id);
                setCurrentTransactionContext(branchedContext);
            }
        }
    }

    public void resume() {
        checkStrandCancelled();
        if (!this.isIsolated && !scheduler.globalNonIsolatedLock.isHeldByCurrentThread()) {
            this.scheduler.globalNonIsolatedLock.lock();
        }
    }

    public void yield() {
        checkStrandCancelled();
        if (!this.isIsolated && scheduler.globalNonIsolatedLock.isHeldByCurrentThread()) {
            this.scheduler.globalNonIsolatedLock.unlock();
        }
    }

    public void done() {
        if (!this.isIsolated && scheduler.globalNonIsolatedLock.isHeldByCurrentThread()) {
            this.scheduler.globalNonIsolatedLock.unlock();
        }
    }

    public boolean isRunnable() {
        return this.isIsolated || this.scheduler.globalNonIsolatedLock.isHeldByCurrentThread();
    }


    private TransactionLocalContext createTrxContextBranch(TransactionLocalContext currentTrxContext,
                                                           int strandName) {
        TransactionLocalContext trxCtx = TransactionLocalContext
                .createTransactionParticipantLocalCtx(currentTrxContext.getGlobalTransactionId(),
                        currentTrxContext.getURL(), currentTrxContext.getProtocol(),
                        currentTrxContext.getInfoRecord());
        String currentTrxBlockId = currentTrxContext.getCurrentTransactionBlockId();
        if (currentTrxBlockId.contains("_")) {
            // remove the parent strand id from the transaction block id
            currentTrxBlockId = currentTrxBlockId.split("_")[0];
        }
        trxCtx.addCurrentTransactionBlockId(currentTrxBlockId + "_" + strandName);
        trxCtx.setTransactionContextStore(currentTrxContext.getTransactionContextStore());
        return trxCtx;
    }

    public Object getProperty(String key) {
        return this.globalProps.get(key);
    }

    public void setProperty(String key, Object value) {
        this.globalProps.put(key, value);
    }

    public boolean isInTransaction() {
        return this.currentTrxContext != null && this.currentTrxContext.isTransactional();
    }

    public void removeCurrentTrxContext() {
        if (!this.trxContexts.isEmpty()) {
            this.currentTrxContext = this.trxContexts.pop();
            this.globalProps.put(CURRENT_TRANSACTION_CONTEXT_PROPERTY, this.currentTrxContext);
            return;
        }
        this.globalProps.remove(CURRENT_TRANSACTION_CONTEXT_PROPERTY);
        this.currentTrxContext = null;
    }

    public void setCurrentTransactionContext(TransactionLocalContext ctx) {
        if (this.currentTrxContext != null) {
            this.trxContexts.push(this.currentTrxContext);
        }
        this.currentTrxContext = ctx;
        this.globalProps.putIfAbsent(CURRENT_TRANSACTION_CONTEXT_PROPERTY, this.currentTrxContext);
    }

    public int getId() {
        return this.id;
    }

    public void checkStrandCancelled() {
        if (this.cancelled) {
            throw ErrorUtils.createCancelledFutureError();
        }
    }
}
