package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoveryManager {
    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private Map<String, TransactionLogRecord> transactionsToRecover;
//    private Map<String, TransactionLogRecord> failedParticipantsToRecover; // needed later?
    private Map<XAResource, ArrayList<TransactionLogRecord>> failedToRecoverResources;
    private Map<String, TransactionLogRecord> failedTransactions;
    private Collection<XAResource> failedToRecoverResources;
    private Collection<XAResource> xaResources;


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

    public void recoverXAResource(XAResource xaResource) {
        transactionsToRecover = TransactionResourceManager.getFileRecoveryLog().getPendingLogs();
        if (transactionsToRecover == null) {
            return;
        }
        if (transactionsToRecover.isEmpty()) {
            return;
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

        Iterable<TransactionLogRecord> iterablePLogs = transactionsToRecover.values();
        boolean recoverSuccessful = false;
        try {
            Map<String, Xid> recoveredXids = retrievePreparedXids(xaResource);
            if (recoveredXids.isEmpty()) {
                System.out.println("No in-doubt transactions in XA Resource.");
                return;
            }
            for (TransactionLogRecord pLog : iterablePLogs) {
                Xid currentXid = XIDGenerator.createXID(pLog.getCombinedId());
                switch (pLog.getTransactionState()) {
                    case COMMITTING:
                        recoverSuccessful = replayCommit(xaResource, currentXid);
                        break;
                    case ABORTING:
                        recoverSuccessful = handleAbort(xaResource, currentXid);
                        break;
                    case COMMITTED, ABORTED:
                        forgetXidInXaResource(currentXid, xaResource);
                        recoverSuccessful = true;
                        break;
                    case MIXED:
                        System.out.println("Transaction" + pLog.getCombinedId() + " in mixed state. " +
                                "Should be handled.");
                        break;
                    case HAZARD:
                        System.out.println("Transaction" + pLog.getCombinedId() + " in hazard state. " +
                                "Check your data for consistency.");
                        break;
                    default:
                        log.error("Transaction" + pLog.getCombinedId() + " in invalid state: " + pLog.getTransactionState());
                        //TODO: handle properly
                }
                if (recoverSuccessful){

//                    if (failedToRecoverResources.containsKey(xaResource)) {
//                        if (failedToRecoverResources.get(xaResource).contains(pLog)) {
//                            failedToRecoverResources.get(xaResource).remove(pLog);
//                            if (failedToRecoverResources.get(xaResource).isEmpty()) {
//                                failedToRecoverResources.remove(xaResource);
//                            }
//                        }
//                    }
//                } else {
//                    if (!failedToRecoverResources.containsKey(xaResource)) {
//                        failedToRecoverResources.put(xaResource, new ArrayList<>());
//                    }
//                    failedToRecoverResources.get(xaResource).add(pLog);
                }
            }
        } catch (XAException ex) {
            System.out.println("Error while recovering XA Resource: " + ex.getMessage());
            log.error("Error while recovering XA Resource: " + ex.getMessage());
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
                    break;
                case HAZARD:
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
                    log.error("Error while replaying commit for transaction: " + xid + " " + e.getMessage());
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
                    System.out.println("Error while replaying abort for transaction: " + xid + " " + e.getMessage());
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
                    System.out.println("Transaction was heuristically committed: " + xid + " " + e.getMessage());
                    canForget = false;
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURRB:
                if(decisionCommit) {
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURMIX:
                System.out.println("Transaction was heuristically mixed: " + xid + " " + e.getMessage());
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURRB:
                if(decisionCommit) {
                    System.out.println("Transaction was heuristically rolled back: " + xid + " " + e.getMessage());
                    canForget = false;
                }
                forgetXidInXaResource(xid, xaResource);
            case XAException.XA_HEURHAZ:
                canForget = false;
                break;
            default:
                break;
        }
        return canForget;
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
