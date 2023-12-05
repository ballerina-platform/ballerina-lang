package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoveryManager {
    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private Map<String, TransactionLogRecord> failedTransactionsToRecover;
    List<XAResource> resourcesToRecover = new ArrayList<>();

    public RecoveryManager() {
        this.failedTransactionsToRecover = new HashMap<>();
    }

    public void addXAResourceToBeRecovered(XAResource xaResource) {
        resourcesToRecover.add(xaResource);

    }

    public void recoverXAResource(XAResource xaResource) {
        failedTransactionsToRecover = TransactionResourceManager.getFileRecoveryLog().getPendingLogs();
        if (failedTransactionsToRecover == null) {
            System.out.println("No failed transactions to recover.");
            return;
        }
        if (failedTransactionsToRecover.isEmpty()) {
            System.out.println("No failed transactions to recover.");
            return;
        }
        Iterable<TransactionLogRecord> iterablePLogs = failedTransactionsToRecover.values();

        boolean recoverComplete = false;
        try {
            Map<String, Xid> recoveredXids = recoverXids(xaResource);
            if (recoveredXids.isEmpty()) {
                System.out.println("No in-doubt transactions in XA Resource.");
                return;
            }
            for (TransactionLogRecord pLog : iterablePLogs) {
                Xid currentXid = recoveredXids.get(pLog.getCombinedId());
                if (currentXid == null) {
                    System.out.println("No in-doubt transaction in XA Resource for transaction id: " + pLog.getTransactionId());
                    continue;
                }
                switch (pLog.getTransactionStatus()) {
                    case STARTING:
                        // no vote has been sent yet
                        // all RMs will abort or are already unilaterally aborted
                        // no need to send any requests
                        break;
                    case PREPARING:
                        // since no decision has been made yet, decide to abort
                        // resource managers should abort unilaterally
                        recoverComplete = abortTransaction(xaResource, currentXid);
                        notifyAbortToUser(pLog, currentXid, xaResource);
                        break;
                    case COMMITTING:
                        // some RMs may be in-doubt
                        // send the decision to commit to all RMs
                        // the participants that are not in-doubt should ignore this request.
                        recoverComplete = replayCommit(xaResource, currentXid);
                        break;
                    case ABORTING:
                        // same as committing case, but send abort decision
                        recoverComplete = handleAbort(xaResource, currentXid);
                        break;
                    case COMMITTED, ABORTED:
                        // transaction has already ended
                        xaResource.end(currentXid, XAResource.TMSUCCESS);
                        xaResource.forget(currentXid);
                        break;
                    case TERMINATED:
                        // transaction has already ended
                        break;
                    default:
                        log.error("Invalid transaction state: " + pLog.getTransactionStatus());
                }
                if (!recoverComplete) {

                }
            }
            if (recoverComplete) {
                forgetLogs();
            }
        } catch (XAException ex) {
            log.error("Error while recovering XA Resource: " + ex.getMessage());
        }
    }

    private void notifyAbortToUser(TransactionLogRecord pLog, Xid currentXid, XAResource xaResource) {

    }

    private void forgetLogs() {
        TransactionResourceManager.getFileRecoveryLog().cleanUpAfterRecovery();
    }

    private Map<String, Xid> recoverXids(XAResource xaResource) throws XAException {
        Xid[] xidsFromScan = null;
        Map<String, Xid> recoveredXids = new HashMap<>();

        xidsFromScan = xaResource.recover(XAResource.TMSTARTRSCAN);
        if (xidsFromScan != null) {
            for (Xid xid : xidsFromScan) {
                if (xid == null) {
                    continue;
                }
                String globalTransactionIdStr = new String(xid.getGlobalTransactionId());
                if (recoveredXids.containsKey(globalTransactionIdStr)) {
                    continue;
                }
                recoveredXids.put(globalTransactionIdStr, xid);
            }
        }
        return recoveredXids;
    }


    private boolean replayCommit(XAResource xaResource, Xid xid) {
        boolean forgetInLog = false;
        try {
            xaResource.commit(xid, false);
            forgetInLog = true;
        } catch (XAException e) {
            switch (e.errorCode){
                case XAException.XA_HEURCOM, XAException.XA_HEURHAZ, XAException.XA_HEURMIX, XAException.XA_HEURRB:
                    log.error("Transaction was heuristically terminated : " + xid + " " + e.getMessage());
                    // handle heuristic termination by resource
                    forgetInLog = true;
                    break;
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    log.error("Invalid Xid: " + xid + " " + e.getMessage());
                    forgetInLog = true;
                    break;
            }
            log.error("Error while replaying commit for transaction: " + xid + " " + e.getMessage());
        }
        return forgetInLog;
    }


    private boolean handleAbort(XAResource xaResource, Xid xid) {
        boolean forgetInLog = false;
        try {
            xaResource.rollback(xid);
            forgetInLog = true;
        } catch (XAException e) {
            switch (e.errorCode){
                case XAException.XA_HEURCOM, XAException.XA_HEURHAZ, XAException.XA_HEURMIX, XAException.XA_HEURRB:
                    log.error("Transaction was heuristically terminated: " + xid + " " + e.getMessage());
                    // handle heuristic termination by resource
                    break;
                case XAException.XAER_NOTA, XAException.XAER_INVAL:
                    log.error("Forgetting transaction. Invalid Xid: " + xid + " " + e.getMessage());
                    forgetInLog = true;
                    break;
            }
            log.error("Error while replaying commit for transaction: " + xid + " " + e.getMessage());
            forgetInLog = false;
        }
        return forgetInLog;
    }

}
