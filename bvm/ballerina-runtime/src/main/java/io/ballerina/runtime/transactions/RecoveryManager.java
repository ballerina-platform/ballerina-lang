/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org).
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

import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * {@code RecoveryManager} is responsible for recovering failed transactions and resources.
 *
 * @since 2201.9.0
 */
public class RecoveryManager {

    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private final Map<String, TransactionLogRecord> failedTransactions;
    private final Collection<XAResource> xaResources;
    private final RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
    private static final PrintStream stderr = System.err;

    public RecoveryManager() {
        this.failedTransactions = new HashMap<>();
        this.xaResources = new ArrayList<>();
    }

    /**
     * Add a transaction log record to the list of records to recover.
     *
     * @param transactionLogRecords the record to add
     */
    private void putFailedTransactionRecords(Map<String, TransactionLogRecord> transactionLogRecords) {
        failedTransactions.putAll(transactionLogRecords);
    }

    /**
     * Get the list of transaction log records that are waiting to be recovered.
     *
     * @return the list of transaction log records to recover
     */
    public Map<String, TransactionLogRecord> getPendingTransactionRecords() {
        return this.failedTransactions;
    }

    /**
     * Add a xa resource to the list of resources to recover. Recoverable XAResources should be added from the relevant
     * library side during their initialization.
     *
     * @param xaResource the resource to be recovered
     */
    public void addXAResourceToRecover(XAResource xaResource) {
        try {
            for (XAResource xaResc : xaResources) {
                if (xaResc.isSameRM(xaResource)) {
                    return;
                }
            }
        } catch (XAException e) {
            // can ignore, we are adding it to array anyway.
        }
        xaResources.add(xaResource);
    }

    /**
     * Perform a recovery pass to recover all failed transactions in xa resources.
     *
     * @return true if recovery pass is successful, false otherwise
     */
    public boolean performRecoveryPass() {
        boolean recoverSuccess = true; // assume success, until it is not

        // Get all the transaction records without terminated logs;
        putFailedTransactionRecords(
                TransactionResourceManager.getInstance().getLogManager().getFailedTransactionLogs());

        Iterator<Map.Entry<String, TransactionLogRecord>> iterator = failedTransactions.entrySet().iterator();
        while (iterator.hasNext()) {
            TransactionLogRecord logRecord = iterator.next().getValue();
            switch (logRecord.getTransactionState()) {
                case PREPARING, COMMITTING, ABORTING, COMMITTED, ABORTED -> {
                    // if the transaction was in any of the terminating states, it means that the 2pc has initiated
                    // and, it has impacted the resources. Therefore, we need to recover the transaction accordingly.
                    Xid xid = XIDGenerator.createXID(logRecord.getCombinedId());
                    boolean singleTrxRecoverSuccess =
                            recoverFailedTrxInAllResources(xid, logRecord.getTransactionState());
                    if (singleTrxRecoverSuccess) {
                        // put a terminated log record to indicate that the transaction was recovered successfully
                        TransactionLogRecord terminatedRecord = new TransactionLogRecord(
                                logRecord.getTransactionId(), logRecord.getTransactionBlockId(),
                                RecoveryState.TERMINATED);
                        TransactionResourceManager.getInstance().getLogManager().put(terminatedRecord);
                        iterator.remove();
                    }
                    recoverSuccess = recoverSuccess && singleTrxRecoverSuccess;
                }
                case MIXED, HAZARD -> {
                    // if the transaction was in any of the mixed or hazard states, it means that the transaction is
                    // not in a state we can handle and should be recovered manually, so we inform the user.
                    String combinedId = logRecord.getCombinedId();
                    switch (logRecord.getTransactionState()) {
                        case MIXED -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_MIXED_STATE, null, combinedId);
                        case HAZARD -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HAZARD_STATE, null, combinedId);
                    }
                    iterator.remove(); // consider it handled, as we have warned the user
                }
            }
        }

        if (!recoverSuccess) {
            diagnosticLog.error(ErrorCodes.TRANSACTION_STARTUP_RECOVERY_FAILED, null);
        }

        // notify the user of all startup recovery warns and errors
        if (!diagnosticLog.getDiagnosticList().isEmpty()) {
            RuntimeUtils.handleDiagnosticErrors(diagnosticLog);
        }
        return recoverSuccess;
    }

    /**
     * Recovers a failed transactions in all XAResources.
     *
     * @param xid   the xid of the transaction
     * @param state the state of the transaction
     * @return true if all transactions are recovered successfully from all resources, false otherwise
     */
    private boolean recoverFailedTrxInAllResources(Xid xid, RecoveryState state) {
        boolean allResourcesRecovered = true;
        for (XAResource xaResource : xaResources) {
            boolean recoveredResource = recoverFailedTrxInXAResource(xaResource, xid, state);
            allResourcesRecovered = recoveredResource && allResourcesRecovered;
        }
        return allResourcesRecovered;
    }

    private boolean recoverFailedTrxInXAResource(XAResource xaResource, Xid xid, RecoveryState state) {
        return switch (state) {
            case PREPARING, ABORTING -> handleAbort(xaResource, xid);
            case COMMITTING -> replayCommit(xaResource, xid);
            case COMMITTED, ABORTED -> {
                forgetXidInXaResource(xid, xaResource);
                yield true;
            }
            default -> false;
        };
    }

    /**
     * Handle commit of a transaction in transaction recovery.
     *
     * @param xaResource the resource that the transaction is associated with
     * @param xid        the xid of the transaction
     * @return true if the commit is successful and the log can be forgotten, false otherwise
     */
    private boolean replayCommit(XAResource xaResource, Xid xid) {
        try {
            xaResource.commit(xid, false);
            return true;
        } catch (XAException e) {
            return switch (e.errorCode) {
                // case: transaction already heuristically terminated by resource
                case XAException.XA_HEURCOM, XAException.XA_HEURHAZ, XAException.XA_HEURMIX, XAException.XA_HEURRB ->
                        handleHeuristicTermination(xid, xaResource, e, true);
                // case : transaction terminated in resource by a concurrent commit; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL -> true;
                default -> {
                    log.error("transient error while replaying commit for transaction: " + xid + " " + e.getMessage());
                    yield false;
                }
            };
        }
    }

    /**
     * Handle abort of a transaction in transaction recovery.
     *
     * @param xaResource the resource that the transaction is associated with
     * @param xid        the xid of the transaction
     * @return true if the abort is successful and the log can be forgotten, false otherwise
     */
    private boolean handleAbort(XAResource xaResource, Xid xid) {
        try {
            xaResource.rollback(xid);
            return true;
        } catch (XAException e) {
            return switch (e.errorCode) {
                // case: transaction already heuristically terminated by resource
                case XAException.XA_HEURCOM, XAException.XA_HEURHAZ, XAException.XA_HEURMIX, XAException.XA_HEURRB ->
                        handleHeuristicTermination(xid, xaResource, e, false);
                // case : transaction terminated in resource by a concurrent rollback; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL -> true;
                default -> {
                    log.error("transient error while replaying abort for transaction: " + xid + " " + e.getMessage());
                    yield false;
                }
            };
        }
    }

    /**
     * Handle heuristic termination of a transaction.
     *
     * @param xid            the xid of the transaction
     * @param xaResource     the resource that the transaction is associated with
     * @param e              the XAException that was thrown
     * @param decisionCommit the decision that was made for that specific transaction
     * @return true if the log can be forgotten, false otherwise
     * <p>
     * "heuristic" itself means "by hand", and that is the way that these outcomes have to be handled. Consider the
     * following possible cases:
     * 1. If the decision was to commit, the transaction is heuristically committed, the log
     * can be forgotten.
     * 2. If the decision was to rollback, the transaction is heuristically rolled back, the log can
     * be forgotten.
     * 3. If the decision was to commit, but the transaction is heuristically rolled back, or vise versa,
     * it needs to be handled manually.
     * 4. If the decision was to commit/rollback, but the transaction is heuristically
     * mixed, or is in hazard state, it needs to be handled manually.
     */
    private boolean handleHeuristicTermination(Xid xid, XAResource xaResource, XAException e, boolean decisionCommit) {
        boolean shouldForgetXid = true;

        switch (e.errorCode) {
            case XAException.XA_HEURCOM:
                if (!decisionCommit) {
                    reportUserOfHeuristics(e, xid, decisionCommit);
                }
                break;
            case XAException.XA_HEURRB:
                if (decisionCommit) {
                    reportUserOfHeuristics(e, xid, decisionCommit);
                }
                break;
            case XAException.XA_HEURMIX:
                reportUserOfHeuristics(e, xid, decisionCommit);
                break;
            case XAException.XA_HEURHAZ:
                reportUserOfHeuristics(e, xid, decisionCommit);
                shouldForgetXid = false;
                break;
        }

        if (shouldForgetXid) {
            forgetXidInXaResource(xid, xaResource);
        }
        return shouldForgetXid;
    }

    /**
     * Reports the user of heuristic termination of a transaction.
     *
     * @param e             the XAException that was thrown
     * @param xid           the xid of the transaction
     * @param decisionCommit the decision that was made for that specific transaction
     */
    private void reportUserOfHeuristics(XAException e, Xid xid, boolean decisionCommit) {
        String transactionID = new String(xid.getGlobalTransactionId());
        String transactionBlockId = new String(xid.getBranchQualifier());
        String combinedId = transactionID + ":" + transactionBlockId;
        String decision = decisionCommit ? "commit" : "rollback";
        switch (e.errorCode) {
            case XAException.XA_HEURCOM -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                    combinedId, "heuristic commit", decision);
            case XAException.XA_HEURRB -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                    combinedId, "heuristic rollback", decision);
            case XAException.XA_HEURMIX -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                    combinedId, "heuristic mixed", decision);
            case XAException.XA_HEURHAZ -> diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                    combinedId, "heuristic hazard", decision);
        }
    }

    /**
     * Forgets a xid in a xa resource.
     *
     * @param xid        the xid to forget
     * @param xaResource the resource to forget the xid in
     */
    private void forgetXidInXaResource(Xid xid, XAResource xaResource) {
        try {
            xaResource.forget(xid);
        } catch (XAException e) {
            // ignore. worst case, heuristic xid is present again on next recovery scan
        }
    }
}
