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
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
import javax.transaction.xa.XAResource;

import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_NAME;
import static io.ballerina.runtime.transactions.TransactionConstants.TRANSACTION_PACKAGE_VERSION;
import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
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
    private static final Logger log = LoggerFactory.getLogger(TransactionResourceManager.class);
    private Map<String, List<BallerinaTransactionContext>> resourceRegistry;
    private Map<String, Transaction> trxRegistry;

    private Map<String, List<BFunctionPointer>> committedFuncRegistry;
    private Map<String, List<BFunctionPointer>> abortedFuncRegistry;

    private ConcurrentSkipListSet<String> failedResourceParticipantSet = new ConcurrentSkipListSet<>();
    private ConcurrentSkipListSet<String> failedLocalParticipantSet = new ConcurrentSkipListSet<>();
    private ConcurrentHashMap<String, ConcurrentSkipListSet<String>> localParticipants = new ConcurrentHashMap<>();

    public Map<BArray, Object> transactionInfoMap;

    private TransactionResourceManager() {
        resourceRegistry = new HashMap<>();
        trxRegistry = new HashMap<>();
        committedFuncRegistry = new HashMap<>();
        abortedFuncRegistry = new HashMap<>();
        transactionInfoMap = new HashMap<>();
        userTransactionManager = new UserTransactionManager();
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
     * @param committed          function pointer to invoke when this transaction committed
     * @param aborted            function pointer to invoke when this transaction aborted
     * @param strand             ballerina strand of the participant
     * @since 0.990.0
     */
    public void registerParticipation(String gTransactionId, String transactionBlockId, BFunctionPointer committed,
                                      BFunctionPointer aborted, Strand strand) {
        localParticipants.computeIfAbsent(gTransactionId, gid -> new ConcurrentSkipListSet<>()).add(transactionBlockId);

        TransactionLocalContext transactionLocalContext = strand.currentTrxContext;
        registerCommittedFunction(transactionBlockId, committed);
        registerAbortedFunction(transactionBlockId, aborted);
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
          return true;
    }

    /**
     * This method acts as the callback which commits all the resources participated in the given transaction.
     *
     * @param strand      the strand
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @return the status of the commit operation
     */
    public boolean notifyCommit(Strand strand, String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        boolean commitSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);
        if (txContextList != null) {
            Transaction trx = trxRegistry.get(combinedId);
            try {
                if (trx != null) {
                    trx.commit();
                }

            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | RollbackException e) {
                log.error("error when committing the transaction, " + combinedId + ":" + e.getMessage(), e);
                commitSuccess = false;
            }

            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    if (xaResource == null) {
                        ctx.commit();
                    }
                } catch (Throwable e) {
                    log.error("error when committing the transaction, " + combinedId + ":" + e.getMessage(), e);
                    commitSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        }
        invokeCommittedFunction(strand, transactionId, transactionBlockId);
        removeContextsFromRegistry(combinedId, transactionId);
        failedResourceParticipantSet.remove(transactionId);
        failedLocalParticipantSet.remove(transactionId);
        localParticipants.remove(transactionId);
        return commitSuccess;
    }

    /**
     * This method acts as the callback which aborts all the resources participated in the given transaction.
     *
     * @param strand the strand
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     * @param error the cause of abortion
     * @return the status of the abort operation
     */
    public boolean notifyAbort(Strand strand, String transactionId, String transactionBlockId, Object error) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
        boolean abortSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(combinedId);

        if (txContextList != null) {
            Transaction trx = trxRegistry.get(combinedId);
            try {
                if (trx != null) {
                    trx.rollback();
                }

            } catch (SystemException e) {
                log.error("error when aborting the transaction, " + combinedId + ":" + e.getMessage(), e);
                abortSuccess = false;
            }

            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    if (xaResource == null) {
                        ctx.rollback();
                    }
                } catch (Throwable e) {
                    log.error("error when aborting the transaction, " + combinedId + ":" + e.getMessage(), e);
                    abortSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        }
        //For the retry  attempt failures the aborted function should not be invoked. It should invoked only when the
        //whole transaction aborts after all the retry attempts.

        // todo: Temporaraly disabling abort functions as there is no clear way to separate rollback and full abort.

        invokeAbortedFunction(strand, transactionId, transactionBlockId, error);
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
        Transaction trx = trxRegistry.get(combinedId);
        try {
            if (trx == null) {
                userTransactionManager.begin();

                trx = userTransactionManager.getTransaction();
                trxRegistry.put(combinedId, trx);
            }

            trx.enlistResource(xaResource);
        } catch (RollbackException | SystemException | NotSupportedException e) {
            log.error("error in initiating the transaction, " + combinedId + ":" + e.getMessage(), e);
        }
    }

    /**
     * This method marks the end of a transaction for the given transaction id.
     *
     * @param transactionId      the global transaction id
     * @param transactionBlockId the block id of the transaction
     */
    void endXATransaction(String transactionId, String transactionBlockId) {
        String combinedId = generateCombinedTransactionId(transactionId, transactionBlockId);
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
                    } catch (Throwable e) {
                        throw new BallerinaException(
                                "error in ending the XA transaction: id: " + combinedId + " error:" + e.getMessage());
                    }
                }
            }
        }
    }

    void rollbackTransaction(Strand strand, String transactionId, String transactionBlockId, Object error) {
        endXATransaction(transactionId, transactionBlockId);
        notifyAbort(strand, transactionId, transactionBlockId, error);
    }

    private void removeContextsFromRegistry(String transactionCombinedId, String gTransactionId) {
        resourceRegistry.remove(transactionCombinedId);
        trxRegistry.remove(transactionCombinedId);
    }

    private String generateCombinedTransactionId(String transactionId, String transactionBlockId) {
        return transactionId + ":" + transactionBlockId;
    }

    private void invokeCommittedFunction(Strand strand, String transactionId, String transactionBlockId) {
        List<BFunctionPointer> fpValueList = committedFuncRegistry.get(transactionId);
        Object[] args = { strand, strand.currentTrxContext.getInfoRecord(), true };
        if (fpValueList != null) {
            for (int i = fpValueList.size(); i > 0; i--) {
                BFunctionPointer fp = fpValueList.get(i - 1);
                //TODO: Replace fp.getFunction().apply
                fp.getFunction().apply(args);
            }
        }
    }

    private void invokeAbortedFunction(Strand strand, String transactionId, String transactionBlockId, Object error) {
        List<BFunctionPointer> fpValueList = abortedFuncRegistry.get(transactionId);
        //TODO: Need to pass the retryManager to get the willRetry value.
        Object[] args = { strand, strand.currentTrxContext.getInfoRecord(), true, error, true, false, true };
        if (fpValueList != null) {
            for (int i = fpValueList.size(); i > 0; i--) {
                BFunctionPointer fp = fpValueList.get(i - 1);
                //TODO: Replace fp.getFunction().apply
                fp.getFunction().apply(args);
            }
        }
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
}
