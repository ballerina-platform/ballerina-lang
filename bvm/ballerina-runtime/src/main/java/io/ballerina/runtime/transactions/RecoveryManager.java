package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoveryManager {
    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private Map<String, TransactionLogRecord> transactionsToRecover;
//    private Map<String, TransactionLogRecord> failedParticipantsToRecover; // needed later?
    private Map<XAResource, ArrayList<TransactionLogRecord>> failedToRecoverResources;

    public RecoveryManager() {
        this.transactionsToRecover = new HashMap<>();
        this.failedToRecoverResources = new HashMap<>();
    }

    public void recoverXAResource(XAResource xaResource) {
        transactionsToRecover = TransactionResourceManager.getFileRecoveryLog().getPendingLogs();
        if (transactionsToRecover == null) {
            return;
        }
        if (transactionsToRecover.isEmpty()) {
            return;
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
    }

    public void markAsTerminated() {
        if (transactionsToRecover.isEmpty() && failedToRecoverResources.isEmpty()) {
            return;
        }
        for (TransactionLogRecord pLog : transactionsToRecover.values()) {
            if (failedToRecoverResources.containsKey(pLog)) {
                System.out.println("Transaction " + pLog.getCombinedId() + " could not be recovered. " +
                        "Will retry recovery on next startup/request.");
                continue;
            }
            TransactionLogRecord terminatedRecord = new TransactionLogRecord(pLog.getTransactionId(),
                    pLog.getTransactionBlockId(), RecoveryState.TERMINATED);
            TransactionResourceManager.getFileRecoveryLog().put(terminatedRecord);
        }
    }

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
                    System.out.println("Transaction was heuristically terminated : " + xid + " " + e.getMessage());
                    ret = handleHeuristicTermination(xid, xaResource, e, true);
                    break;
                // case : transaction terminated in resource by a concurrent commit; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    System.out.println("Invalid Xid: " + xid + " " + e.getMessage());
                    ret = true;
                    break;
                default:
                    log.error("Error while replaying commit for transaction: " + xid + " " + e.getMessage());
            }
        }
        return ret;
    }


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
                    System.out.println("Transaction was heuristically terminated: " + xid + " " + e.getMessage());
                    ret = handleHeuristicTermination(xid, xaResource, e, false);
                    break;
                // case : transaction terminated in resource by a concurrent rollback; xid no longer know by resource
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    System.out.println("Forgetting transaction. Invalid Xid: " + xid + " " + e.getMessage());
                    ret = true;
                    break;
                default:
                    System.out.println("Error while replaying abort for transaction: " + xid + " " + e.getMessage());
            }
        }
        return ret;
    }

    private boolean handleHeuristicTermination(Xid xid,
                                                         XAResource xaResource, XAException e, boolean decisionCommit) {
        boolean canForget = true;
        switch (e.errorCode) {
            case XAException.XA_HEURHAZ:
                System.out.println("Transaction in hazard state: " + xid + " " + e.getMessage());
                canForget = false;
                break;
            case XAException.XA_HEURCOM:
                if(!decisionCommit){
                    System.out.println("Transaction was heuristically committed: " + xid + " " + e.getMessage());
                    canForget = false;
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            case XAException.XA_HEURMIX:
                System.out.println("Transaction was heuristically mixed: " + xid + " " + e.getMessage());
                forgetXidInXaResource(xid, xaResource);
                // TODO : handle mixed state recovery
                break;
            case XAException.XA_HEURRB:
                if(decisionCommit) {
                    System.out.println("Transaction was heuristically rolled back: " + xid + " " + e.getMessage());
                    canForget = false;
                }
                forgetXidInXaResource(xid, xaResource);
                break;
            default:
                break;
        }
        return canForget;
    }

    private void forgetXidInXaResource(Xid xid, XAResource xaResource) {
        try {
            xaResource.forget(xid);
        } catch (XAException e) {
            System.out.println("Unexpected error during forget" + e + "ignoring");
            // ignore: worst case, heuristic xid is presented again on next recovery scan

        }
    }

}
