package io.ballerina.runtime.transactions;

import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoveryManager {
    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private Map<String, TransactionLogRecord> failedTransactions;
    private Collection<XAResource> failedToRecoverResources;
    private Collection<XAResource> xaResources;

    private final RuntimeDiagnosticLog diagnosticLog;
    private static final PrintStream stderr = System.err;

    public RecoveryManager() {
        this.failedTransactions = new HashMap<>();
        this.failedToRecoverResources = new ArrayList<>();
        this.xaResources = new ArrayList<>();
        this.diagnosticLog = new RuntimeDiagnosticLog();
    }

    /**
     * Add a transaction log record to the list of records to recover.
     * @param transactionLogRecords the record to add
     */
    private void putFailedTransactionRecords(Map<String, TransactionLogRecord> transactionLogRecords) {
        failedTransactions.putAll(transactionLogRecords);
    }

    /**
     * Get the list of transaction log records that are waiting to be recovered.
     * @return the list of transaction log records to recover
     */
    public Map<String, TransactionLogRecord> getPendingTransactionRecords() {
        return this.failedTransactions;
    }

    /**
     * Add a xa resource to the list of resources to recover.
     * XAResources should be added from the relavent library side during their initialization.
     * @param xaResource the resource to add
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

    public boolean performRecoveryPass(){
        boolean allOk = true;

        // Get all the transaction records without terminated logs;
        putFailedTransactionRecords(TransactionResourceManager.getInstance().getLogManager().getFailedTransactionLogs());

        // Warn user of hazards and mixed outcomes
        for (TransactionLogRecord logRecord : failedTransactions.values()) {
            String combinedId = logRecord.getCombinedId();
            switch (logRecord.getTransactionState()) {
                case MIXED:
                    diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_MIXED_STATE, null, combinedId);
                    break;
                case HAZARD:
                    diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HAZARD_STATE, null, combinedId);
                    break;
                default:
                    break;
            }
        }

        // Recover all the recoverable XA Resources
        if (!xaResources.isEmpty()){
            // Get prepared Xids from all the resources
            Map<XAResource, Map<String, Xid>> preparedXids = new HashMap<>();
            for (XAResource xaResource : xaResources) {
                Map<String, Xid> recoveredXids;
                try {
                    recoveredXids = retrievePreparedXids(xaResource);
                } catch (XAException e) {
                    diagnosticLog.warn(ErrorCodes.TRANSACTION_CANNOT_COLLECT_XIDS_IN_RESOURCE, null, xaResource);
                    failedToRecoverResources.add(xaResource);
                    continue;
                }
                preparedXids.put(xaResource, recoveredXids);
            }

            // Check if all the prepared xids are in the pending logs and recover them
            for (Map.Entry<XAResource, Map<String, Xid>> entry : preparedXids.entrySet()) {
                allOk = allOk && recoverXAResource(entry.getKey(), entry.getValue());
            }
        }

        if (!allOk) {
            diagnosticLog.error(ErrorCodes.TRANSACTION_STARTUP_RECOVERY_FAILED, null);
        }

        // notify the user of all startup recovery warns and errors
        if (!diagnosticLog.getDiagnosticList().isEmpty()) {
            RuntimeUtils.handleDiagnosticErrors(diagnosticLog);
        }
        return allOk;
    }

    /**
     * Recovers all the XAResources
     */
    public boolean recoverXAResource(XAResource xaResource, Map<String, Xid> recoveredXids) {
        boolean allXidsRecovered = true;
        for (Xid xid : recoveredXids.values()) {
            String globalTransactionIdStr = new String(xid.getGlobalTransactionId());
            String branchQualifierStr = new String(xid.getBranchQualifier());
            String combinedIdStr = globalTransactionIdStr + ":" + branchQualifierStr; // combined id
            if (failedTransactions.containsKey(combinedIdStr)) {
                RecoveryState state = failedTransactions.get(combinedIdStr).getTransactionState();
                boolean recovered = recoverFailedTrxInXAResource(xaResource, xid, state);
                if (recovered) {
                    // put a terminated log record to indicate that the transaction is recovered successfully
                    TransactionLogRecord terminatedRecord = new TransactionLogRecord(
                            globalTransactionIdStr, branchQualifierStr, RecoveryState.TERMINATED);
                    TransactionResourceManager.getInstance().getLogManager().put(terminatedRecord);
                    failedTransactions.remove(combinedIdStr);
                } else {
                    allXidsRecovered = false;
                    failedToRecoverResources.add(xaResource);
                }
            }
        }
        return allXidsRecovered;
    }

    private boolean recoverFailedTrxInXAResource(XAResource xaResource, Xid xid, RecoveryState state) {
        boolean recovered = false;
        switch (state) {
            case PREPARING: // failed during prepare, no decision record found
                recovered = handleAbort(xaResource, xid);
                break;
            case COMMITTING:
                recovered = replayCommit(xaResource, xid);
                break;
            case ABORTING:
                recovered = handleAbort(xaResource, xid);
                break;
            case COMMITTED, ABORTED:
                forgetXidInXaResource(xid, xaResource);
                recovered = true;
                break;
            default:
                break;
        }
        return recovered;
    }

    /**
     * Retrieve all prepared xids from a xa resource.
     *
     * @param xaResource the resource to retrieve the xids from
     * @return a map of all xids that are prepared in the resource
     * @throws XAException if an error occurs while retrieving the xids
     */
    private Map<String, Xid> retrievePreparedXids(XAResource xaResource) throws XAException {
        Map<String, Xid> retrievedXids = new HashMap<>();
        ArrayList<Xid> recoverdXidsFromScan = new ArrayList<>();
        
        Xid[] xidsFromScan = null;
        xidsFromScan = xaResource.recover(XAResource.TMSTARTRSCAN);
        while (xidsFromScan != null && xidsFromScan.length > 0) {
            recoverdXidsFromScan.addAll(List.of(xidsFromScan));
            xidsFromScan = (xaResource.recover(XAResource.TMNOFLAGS));
        }
        xidsFromScan = xaResource.recover(XAResource.TMENDRSCAN);
        if (xidsFromScan != null && xidsFromScan.length > 0) {
            recoverdXidsFromScan.addAll(List.of(xidsFromScan));
        }

        if (!recoverdXidsFromScan.isEmpty()) {
            for (Xid xid : recoverdXidsFromScan) {
                if (xid == null) {
                    continue;
                }
                if (xid.getFormatId() != (XIDGenerator.getDefaultFormat())){
                    continue;
                }
                String globalTransactionIdStr = new String(xid.getGlobalTransactionId());
                String branchQualifierStr = new String(xid.getBranchQualifier());
                String combinedIdStr = globalTransactionIdStr + ":" + branchQualifierStr;
                if (retrievedXids.containsKey(combinedIdStr)) {
                    continue;
                }
                retrievedXids.put(combinedIdStr, xid);
            }
        }
        return retrievedXids;
    }

    /**
     * Handle commit of a transaction in transaction recovery
     *
     * @param xaResource the resource that the transaction is associated with
     * @param xid the xid of the transaction
     * @return true if the commit is successful and the log can be forgotten, false otherwise
     */
    private boolean replayCommit(XAResource xaResource, Xid xid) {
        boolean ret = false;
        try {
            xaResource.commit(xid, false);
            ret = true;
        } catch (XAException e) {
            switch (e.errorCode){
                // case: transaction already heuristically terminated by resource
                case XAException.XA_HEURCOM,
                        XAException.XA_HEURHAZ,
                        XAException.XA_HEURMIX,
                        XAException.XA_HEURRB:
                    ret = handleHeuristicTermination(xid, xaResource, e, true);
                    break;
                // case : transaction terminated in resource by a concurrent commit; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    ret = true;
                    break;
                default:
                    log.error("transient error while replaying commit for transaction: " + xid + " " + e.getMessage());
            }
        }
        return ret;
    }

    /**
     * Handle abort of a transaction in transaction recovery
     *
     * @param xaResource the resource that the transaction is associated with
     * @param xid the xid of the transaction
     * @return true if the abort is successful and the log can be forgotten, false otherwise
     */
    private boolean handleAbort(XAResource xaResource, Xid xid) {
        boolean ret = false;
        try {
            xaResource.rollback(xid);
            ret = true;
        } catch (XAException e) {
            switch (e.errorCode){
                // case: transaction already heuristically terminated by resource
                case XAException.XA_HEURCOM,
                        XAException.XA_HEURHAZ,
                        XAException.XA_HEURMIX,
                        XAException.XA_HEURRB:
                    ret = handleHeuristicTermination(xid, xaResource, e, false);
                    break;
                // case : transaction terminated in resource by a concurrent rollback; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    ret = true;
                    break;
                default:
                    log.error("transient error while replaying abort for transaction: " + xid + " " + e.getMessage());

            }
        }
        return ret;
    }


    /**
     * Handle heuristic termination of a transaction.
     *
     * @param xid the xid of the transaction
     * @param xaResource the resource that the transaction is associated with
     * @param e the XAException that was thrown
     * @param decisionCommit the decision that was made for that specific transaction
     * @return true if the log can be forgotten, false otherwise
     *
     * "heuristic" itself means "by hand", and that is the way that these outcomes have to be handled.
     *  Consider the following possible cases:
     *      1. If the decision was to commit, the transaction is heuristically committed, the log can be forgotten.
     *      2. If the decision was to rollback, the transaction is heuristically rolled back, the log can be forgotten.
     *      3. If the decision was to commit, but the transaction is heuristically rolled back,
*                   or vise versa, it needs to be handled manually.
     *      4. If the decision was to commit/rollback, but the transaction is heuristically mixed,
     *              or is in hazard state, it needs to be handled manually.
     */
    private boolean handleHeuristicTermination(Xid xid, XAResource xaResource, XAException e, boolean decisionCommit) {
        boolean canForget = true;
        switch (e.errorCode) {
            case XAException.XA_HEURCOM:
                if(!decisionCommit){
                    reportUserOfHueristics(e, xid, decisionCommit);
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURRB:
                if(decisionCommit) {
                    reportUserOfHueristics(e, xid, decisionCommit);
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURMIX:
                reportUserOfHueristics(e, xid, decisionCommit);
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURHAZ:
                reportUserOfHueristics(e, xid, decisionCommit);
                canForget = false;
                break;
            default:
                break;
        }
        return canForget;
    }

    private void reportUserOfHueristics(XAException e, Xid xid, boolean decisionCommit) {
        String transactionID = new String(xid.getGlobalTransactionId());
        String transactionBlockId = new String(xid.getBranchQualifier());
        String combinedId = transactionID + ":" + transactionBlockId;
        String decision = decisionCommit ? "commit" : "rollback";
        switch (e.errorCode){
            case XAException.XA_HEURCOM:
                diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                        combinedId, "heuristic commit", decision);
                break;
            case XAException.XA_HEURRB:
                diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                        combinedId, "heuristic rollback", decision);
                break;
            case XAException.XA_HEURMIX:
                diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                        combinedId, "heuristic mixed", decision);
                break;
            case XAException.XA_HEURHAZ:
                diagnosticLog.warn(ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE, null,
                        combinedId, "heuristic hazard", decision);
                break;
            default:
                break;
        }
    }

    /**
     * Forgets a xid in a xa resource.
     *
     * @param xid the xid to forget
     * @param xaResource the resource to forget the xid in
     */
    private void forgetXidInXaResource(Xid xid, XAResource xaResource) {
        try {
            xaResource.forget(xid);
        } catch (XAException e) {
            // ignore.. worst case, heuristic xid is presented again on next recovery scan
        }
    }
}
