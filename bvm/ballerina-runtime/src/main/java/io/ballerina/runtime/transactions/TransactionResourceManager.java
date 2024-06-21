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

import com.atomikos.icatch.jta.UserTransactionManager;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigMap;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.transactions.TransactionConstants.DEFAULT_TRX_AUTO_COMMIT_TIMEOUT;
import static io.ballerina.runtime.transactions.TransactionConstants.DEFAULT_TRX_CLEANUP_TIMEOUT;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_ID;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_NAME;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_VERSION;
import static javax.transaction.xa.XAResource.TMNOFLAGS;
import static javax.transaction.xa.XAResource.TMSUCCESS;

/**
 * {@code TransactionResourceManager} registry for transaction contexts.
 *
 * @since 1.0
 */
public class TransactionResourceManager {

    private static TransactionResourceManager transactionResourceManager = null;
    private  static UserTransactionManager userTransactionManager = null;

    private static final StrandMetadata COMMIT_METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX,
            TRANSACTION_PACKAGE_NAME,
            TRANSACTION_PACKAGE_VERSION, "onCommit");
    private static final StrandMetadata ROLLBACK_METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX,
            TRANSACTION_PACKAGE_NAME,
            TRANSACTION_PACKAGE_VERSION, "onRollback");
    private static final String ATOMIKOS_LOG_BASE_PROPERTY = "com.atomikos.icatch.log_base_dir";
    private static final String ATOMIKOS_LOG_NAME_PROPERTY = "com.atomikos.icatch.log_base_name";
    private static final String ATOMIKOS_REGISTERED_PROPERTY = "com.atomikos.icatch.registered";
    public static final String TRANSACTION_AUTO_COMMIT_TIMEOUT_KEY = "transactionAutoCommitTimeout";
    public static final String TRANSACTION_CLEANUP_TIMEOUT_KEY = "transactionCleanupTimeout";

    private static final Logger log = LoggerFactory.getLogger(TransactionResourceManager.class);
    private Map<String, List<BallerinaTransactionContext>> resourceRegistry;
    private Map<String, Transaction> trxRegistry;
    private Map<String, Xid> xidRegistry;

    private Map<String, List<BFunctionPointer>> committedFuncRegistry;
    private Map<String, List<BFunctionPointer>> abortedFuncRegistry;

    private ConcurrentSkipListSet<String> failedResourceParticipantSet = new ConcurrentSkipListSet<>();
    private ConcurrentSkipListSet<String> failedLocalParticipantSet = new ConcurrentSkipListSet<>();
    private ConcurrentHashMap<String, ConcurrentSkipListSet<String>> localParticipants = new ConcurrentHashMap<>();

    private boolean transactionManagerEnabled;
    private static final PrintStream stderr = System.err;

    Map<ByteBuffer, Object> transactionInfoMap;

    private TransactionResourceManager() {
        resourceRegistry = new HashMap<>();
        committedFuncRegistry = new HashMap<>();
        abortedFuncRegistry = new HashMap<>();
        transactionInfoMap = new ConcurrentHashMap<>();
        transactionManagerEnabled = getTransactionManagerEnabled();
        if (transactionManagerEnabled) {
            trxRegistry = new HashMap<>();
            setLogProperties();
            userTransactionManager = new UserTransactionManager();
        } else {
            xidRegistry = new HashMap<>();
        }
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

    /**
     * This method sets values for atomikos transaction log path and name properties using the available configs.
     *
     */
    private void setLogProperties() {
        final Path projectRoot = Paths.get(RuntimeUtils.USER_DIR);
        if (projectRoot != null) {
            String logDir = getTransactionLogDirectory();
            Path logDirPath = Paths.get(logDir);
            Path transactionLogDirectory;
            if (!logDirPath.isAbsolute()) {
                logDir = projectRoot.toAbsolutePath().toString() + File.separatorChar + logDir;
                transactionLogDirectory = Paths.get(logDir);
            } else {
                transactionLogDirectory = logDirPath;
            }
            if (!Files.exists(transactionLogDirectory)) {
                try {
                    Files.createDirectory(transactionLogDirectory);
                } catch (IOException e) {
                    stderr.println("error: failed to create transaction log directory in " + logDir);
                }
            }
            System.setProperty(ATOMIKOS_LOG_BASE_PROPERTY, logDir);
            System.setProperty(ATOMIKOS_LOG_NAME_PROPERTY, "transaction_recovery");
            System.setProperty(ATOMIKOS_REGISTERED_PROPERTY, "not-registered");
        }
    }

    /**
     * This method checks whether the atomikos transaction manager should be enabled or not.
     *
     * @return boolean whether the atomikos transaction manager should be enabled or not
     */
    public boolean getTransactionManagerEnabled() {
        VariableKey managerEnabledKey = new VariableKey(TRANSACTION_PACKAGE_ID, "managerEnabled",
                PredefinedTypes.TYPE_BOOLEAN, false);
        if (!ConfigMap.containsKey(managerEnabledKey)) {
            return false;
        } else {
            return (boolean) ConfigMap.get(managerEnabledKey);
        }
    }

    /**
     * This method gets the user specified config for log directory name.
     *
     * @return string log directory name
     */
    private String getTransactionLogDirectory() {
        VariableKey logKey = new VariableKey(TRANSACTION_PACKAGE_ID, "logBase", PredefinedTypes.TYPE_STRING, false);
        if (!ConfigMap.containsKey(logKey)) {
            return "transaction_log_dir";
        } else {
            return ((BString) ConfigMap.get(logKey)).getValue();
        }
    }

    /**
     * This method gets the user specified config for the transaction auto commit timeout. Default is 120.
     *
     * @return int transaction auto commit timeout value
     */
    public static int getTransactionAutoCommitTimeout() {
        VariableKey transactionAutoCommitTimeoutKey = new VariableKey(TRANSACTION_PACKAGE_ID,
                TRANSACTION_AUTO_COMMIT_TIMEOUT_KEY, PredefinedTypes.TYPE_INT, false);
        if (!ConfigMap.containsKey(transactionAutoCommitTimeoutKey)) {
            return DEFAULT_TRX_AUTO_COMMIT_TIMEOUT;
        } else {
            Object configValue = ConfigMap.get(transactionAutoCommitTimeoutKey);
            if (configValue == null) {
                return DEFAULT_TRX_AUTO_COMMIT_TIMEOUT;
            }
            return parseTimeoutValue(configValue, DEFAULT_TRX_AUTO_COMMIT_TIMEOUT);
        }
    }

    /**
     * This method gets the user specified config for cleaning up dead transactions. Default is 600.
     *
     * @return int transaction cleanup after value
     */
    public static int getTransactionCleanupTimeout() {
        VariableKey transactionCleanupTimeoutKey = new VariableKey(TRANSACTION_PACKAGE_ID,
                TRANSACTION_CLEANUP_TIMEOUT_KEY,
                PredefinedTypes.TYPE_INT, false);
        if (!ConfigMap.containsKey(transactionCleanupTimeoutKey)) {
            return DEFAULT_TRX_AUTO_COMMIT_TIMEOUT;
        } else {
            Object configValue = ConfigMap.get(transactionCleanupTimeoutKey);
            if (configValue == null) {
                return DEFAULT_TRX_CLEANUP_TIMEOUT;
            }
            return parseTimeoutValue(configValue, DEFAULT_TRX_CLEANUP_TIMEOUT);
        }
    }

    private static int parseTimeoutValue(Object configValue, int defaultValue) {
        if (!(configValue instanceof Number number)) {
            return defaultValue;
        }
        int timeoutValue = number.intValue();
        if (timeoutValue <= 0) {
            return defaultValue;
        }
        return timeoutValue;
    }

    /**
     * This method will register connection resources with a particular transaction.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @param txContext          ballerina transaction context which includes the underlying connection info
     */
    public void register(String transactionId, String transactionBlockId, BallerinaTransactionContext txContext) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        resourceRegistry.computeIfAbsent(combinedId, resourceList -> new ArrayList<>()).add(txContext);
    }

    /**
     * This method will register a committed function handler of a particular transaction.
     *
     * @param transactionBlockId the block id of the transaction
     * @param fpValue   the function pointer for the committed function
     */
    public void registerCommittedFunction(String transactionBlockId, BFunctionPointer fpValue) {
        if (fpValue != null) {
            committedFuncRegistry.computeIfAbsent(transactionBlockId, list -> new ArrayList<>()).add(fpValue);
        }
    }

    /**
     * This method will register an aborted function handler of a particular transaction.
     *
     * @param transactionBlockId the block id of the transaction
     * @param fpValue   the function pointer for the aborted function
     */
    public void registerAbortedFunction(String transactionBlockId, BFunctionPointer fpValue) {
        if (fpValue != null) {
            abortedFuncRegistry.computeIfAbsent(transactionBlockId, list -> new ArrayList<>()).add(fpValue);
        }
    }

    /**
     * Register a participation in a global transaction.
     *
     * @param gTransactionId     global transaction id
     * @param transactionBlockId participant identifier
     * @since 0.990.0
     */
    public void registerParticipation(String gTransactionId, String transactionBlockId) {
        localParticipants.computeIfAbsent(gTransactionId, gid -> new ConcurrentSkipListSet<>()).add(transactionBlockId);

        TransactionLocalContext transactionLocalContext = Scheduler.getStrand().currentTrxContext;
        transactionLocalContext.beginTransactionBlock(transactionBlockId);
    }

    /**
     * This method acts as the callback which notify all the resources participated in the given transaction. 
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @return the status of the prepare operation
     */
    //TODO:Comment for now, might need it for distributed transactions.
    public boolean prepare(String transactionId, String transactionBlockId) {
        endXATransaction(transactionId, transactionBlockId);
        if (transactionManagerEnabled) {
            return true;
        }
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);
        if (txContextList != null) {
            Xid xid = xidRegistry.get(combinedId);
            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    if (xaResource != null) {
                        xaResource.prepare(xid);
                    }
                } catch (XAException e) {
                    log.error("error at transaction prepare phase in transaction " + transactionId
                            + ":" + e.getMessage(), e);
                    return false;
                }
            }
        }

        boolean status = true;
        if (failedResourceParticipantSet.contains(transactionId) || failedLocalParticipantSet.contains(transactionId)) {
            // resource participant reported failure.
            status = false;
        }
        log.info(String.format("Transaction prepare (participants): %s", status ? "success" : "failed"));
        return status;
    }

    /**
     * This method acts as the callback which commits all the resources participated in the given transaction.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @return the status of the commit operation
     */
    public boolean notifyCommit(String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        boolean commitSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);
        if (txContextList != null) {
            if (transactionManagerEnabled) {
                Transaction trx = trxRegistry.get(combinedId);
                try {
                    if (trx != null) {
                        trx.commit();
                    }
                } catch (SystemException | HeuristicMixedException | HeuristicRollbackException
                        | RollbackException e) {
                    log.error("error when committing transaction " + transactionId + ":" + e.getMessage(), e);
                    commitSuccess = false;
                }
            }

            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    if (transactionManagerEnabled && xaResource == null) {
                        ctx.commit();
                    } else {
                        if (xaResource != null) {
                            Xid xid = xidRegistry.get(combinedId);
                            xaResource.commit(xid, false);
                        } else {
                            ctx.commit();
                        }
                    }
                } catch (XAException e) {
                    log.error("error when committing transaction " + transactionId + ":" + e.getMessage(), e);
                    commitSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        }
        return commitSuccess;
    }

    public void cleanTransaction(String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        removeContextsFromRegistry(combinedId, transactionId);
        failedResourceParticipantSet.remove(transactionId);
        failedLocalParticipantSet.remove(transactionId);
        localParticipants.remove(transactionId);
    }

    /**
     * This method acts as the callback which aborts all the resources participated in the given transaction.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @return the status of the abort operation
     */
    public boolean notifyAbort(String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        boolean abortSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);

        if (txContextList != null) {
            if (transactionManagerEnabled) {
                Transaction trx = trxRegistry.get(combinedId);
                try {
                    if (trx != null) {
                        trx.rollback();
                    }
                } catch (SystemException e) {
                    log.error("error when aborting transaction " + transactionId + ":" + e.getMessage(), e);
                    abortSuccess = false;
                }
            }

            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    if (transactionManagerEnabled && xaResource == null) {
                        ctx.rollback();
                    } else {
                        Xid xid = xidRegistry.get(combinedId);
                        if (xaResource != null) {
                            ctx.getXAResource().rollback(xid);
                        } else {
                            ctx.rollback();
                        }
                    }
                } catch (XAException e) {
                    log.error("error when aborting the transaction " + transactionId + ":" + e.getMessage(), e);
                    abortSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        }
        //For the retry  attempt failures the aborted function should not be invoked. It should invoked only when the
        //whole transaction aborts after all the retry attempts.

        // todo: Temporaraly disabling abort functions as there is no clear way to separate rollback and full abort.

        removeContextsFromRegistry(combinedId, transactionId);
        failedResourceParticipantSet.remove(transactionId);
        failedLocalParticipantSet.remove(transactionId);
        localParticipants.remove(transactionId);
        return abortSuccess;
    }

    /**
     * This method starts a transaction for the given xa resource. If there is no transaction is started for the
     * given XID a new transaction is created.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @param xaResource         the XA resource which participates in the transaction
     */
    public void beginXATransaction(String transactionId, String transactionBlockId, XAResource xaResource) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        if (transactionManagerEnabled) {
            Transaction trx = trxRegistry.get(combinedId);
            try {
                if (trx == null) {
                    userTransactionManager.begin();

                    trx = userTransactionManager.getTransaction();
                    trxRegistry.put(combinedId, trx);
                }
            } catch (SystemException | NotSupportedException e) {
                log.error("error in initiating transaction " + transactionId + ":" + e.getMessage(), e);
            }
        } else {
            Xid xid = xidRegistry.get(combinedId);
            if (xid == null) {
                xid = XIDGenerator.createXID();
                xidRegistry.put(combinedId, xid);
            }
            try {
                xaResource.start(xid, TMNOFLAGS);
            } catch (XAException e) {
                log.error("error in starting XA transaction " + transactionId + ":" + e.getMessage(), e);
            }
        }
    }

    /**
     * Cleanup the Info record keeping state related to current transaction context and remove the current
     * context from the stack.
     */
    public void cleanupTransactionContext() {
        Strand strand = Scheduler.getStrand();
        TransactionLocalContext transactionLocalContext = strand.currentTrxContext;
        transactionLocalContext.removeTransactionInfo();
        strand.removeCurrentTrxContext();
    }

    /**
     * This method returns true if there is a failure of the current transaction, otherwise false.
     * @return true if there is a failure of the current transaction.
     */
    public boolean getAndClearFailure() {
        return Scheduler.getStrand().currentTrxContext.getAndClearFailure() != null;
    }

    /**
     * This method is used to get the error which is set by calling setRollbackOnly().
     * If it is not set, then returns null.
     * @return the error or null.
     */
    public Object getRollBackOnlyError() {
        TransactionLocalContext transactionLocalContext = Scheduler.getStrand().currentTrxContext;
        return transactionLocalContext.getRollbackOnly();
    }

    /**
     * This method checks if the current strand is in a transaction or not.
     * @return True if the current strand is in a transaction.
     */
    public boolean isInTransaction() {
        return Scheduler.getStrand().isInTransaction();
    }

    /**
     * This method notify the given transaction to abort.
     * @param transactionBlockId The transaction blockId
     */
    public void notifyTransactionAbort(String transactionBlockId) {
        Scheduler.getStrand().currentTrxContext.notifyAbortAndClearTransaction(transactionBlockId);
    }

    /**
     * This method retrieves the list of rollback handlers.
     * @return Array of rollback handlers
     */
    public BArray getRegisteredRollbackHandlerList() {
        List<BFunctionPointer> abortFunctions =
                abortedFuncRegistry.get(Scheduler.getStrand().currentTrxContext.getGlobalTransactionId());
        if (abortFunctions != null && !abortFunctions.isEmpty()) {
            Collections.reverse(abortFunctions);
            return ValueCreator.createArrayValue(abortFunctions.toArray(),
                    TypeCreator.createArrayType(abortFunctions.get(0).getType()));
        } else {
            return getNillArray();
        }
    }

    /**
     * This method retrieves the list of commit handlers.
     * @return Array of commit handlers
     */
    public BArray getRegisteredCommitHandlerList() {
        List<BFunctionPointer> commitFunctions =
                committedFuncRegistry.get(Scheduler.getStrand().currentTrxContext.getGlobalTransactionId());
        if (commitFunctions != null && !commitFunctions.isEmpty()) {
            Collections.reverse(commitFunctions);
            return ValueCreator.createArrayValue(commitFunctions.toArray(),
                    TypeCreator.createArrayType(commitFunctions.get(0).getType()));
        } else {
            return getNillArray();
        }
    }

    private BArray getNillArray() {
        return ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_NULL));
    }

    /**
     * This method marks the current transaction context as non-transactional.
     */
    public void setContextNonTransactional() {
        //todo check possibility of currentTrxContext being null when this get called
        TransactionLocalContext localContext = Scheduler.getStrand().currentTrxContext;
        if (localContext != null) {
            localContext.setTransactional(false);
        }
    }

    /**
     * This method set the given transaction context as the current transaction context in the stack.
     * @param trxCtx The input transaction context
     */
    public void setCurrentTransactionContext(TransactionLocalContext trxCtx) {
        Scheduler.getStrand().setCurrentTransactionContext(trxCtx);
    }

    /**
     * This method returns the current transaction context.
     * @return The current Transaction Context
     */
    public TransactionLocalContext getCurrentTransactionContext() {
        return Scheduler.getStrand().currentTrxContext;
    }

    /**
     * This method marks the end of a transaction for the given transaction id.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     */
    void endXATransaction(String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        if (transactionManagerEnabled) {
            Transaction trx = trxRegistry.get(combinedId);
            if (trx != null) {
                List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);
                if (txContextList != null) {
                    for (BallerinaTransactionContext ctx : txContextList) {
                        try {
                            XAResource xaResource = ctx.getXAResource();
                            if (xaResource != null) {
                                trx.delistResource(xaResource, TMSUCCESS);
                            }
                        } catch (IllegalStateException | SystemException e) {
                            log.error("error in ending the XA transaction " + transactionId
                                    + ":" + e.getMessage(), e);
                        }
                    }
                }
            }
        } else {
            Xid xid = xidRegistry.get(combinedId);
            List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);
            if (xid != null && txContextList != null) {
                for (BallerinaTransactionContext ctx : txContextList) {
                    try {
                        XAResource xaResource = ctx.getXAResource();
                        if (xaResource != null) {
                            ctx.getXAResource().end(xid, TMSUCCESS);
                        }
                    } catch (XAException e) {
                        log.error("error in ending XA transaction " + transactionId + ":" + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void removeContextsFromRegistry(String transactionCombinedId, String gTransactionId) {
        resourceRegistry.remove(transactionCombinedId);
        if (transactionManagerEnabled) {
            trxRegistry.remove(transactionCombinedId);
        } else {
            xidRegistry.remove(transactionCombinedId);
        }
    }

    private String generateCombinedTransactionId(String transactionId, String transactionBlockId) {
        if (transactionBlockId.contains("_")) {
            // remove the strand id from the transaction block id
            return transactionBlockId.split("_")[0];
        }
        return transactionId + ":" + transactionBlockId;
    }

    public void notifyResourceFailure(String gTransactionId) {
        failedResourceParticipantSet.add(gTransactionId);
        // The resource excepted (uncaught).
        log.info("Trx infected callable unit excepted id : " + gTransactionId);
    }

    public void notifyLocalParticipantFailure(String gTransactionId, String blockId) {
        ConcurrentSkipListSet<String> participantBlockIds = localParticipants.get(gTransactionId);
        if (participantBlockIds != null && participantBlockIds.contains(blockId)) {
            failedLocalParticipantSet.add(gTransactionId);
        }
    }

    public Object getTransactionRecord(BArray xid) {
        synchronized (transactionInfoMap) {
            if (transactionInfoMap.containsKey(ByteBuffer.wrap(xid.getBytes()))) {
                return transactionInfoMap.get(ByteBuffer.wrap(xid.getBytes()));
            }
            return null;
        }
    }
}
