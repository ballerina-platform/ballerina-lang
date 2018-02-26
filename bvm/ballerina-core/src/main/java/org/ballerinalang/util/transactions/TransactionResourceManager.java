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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static javax.transaction.xa.XAResource.TMNOFLAGS;
import static javax.transaction.xa.XAResource.TMSUCCESS;

/**
 * {@code TransactionResourceManager} registry for transaction contexts.
 *
 * @since 0.964.0
 */
public class TransactionResourceManager {

    private static TransactionResourceManager transactionResourceManager = null;
    private static final Logger log = LoggerFactory.getLogger(TransactionResourceManager.class);
    private Map<String, List<BallerinaTransactionContext>> resourceRegistry;
    private Map<String, Xid> xidRegistry;

    private TransactionResourceManager() {
        resourceRegistry = new HashMap<>();
        xidRegistry = new HashMap<>();
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

    public void register(String transactionId, BallerinaTransactionContext txContext) {
        resourceRegistry.computeIfAbsent(transactionId, resourceList -> new ArrayList<>()).add(txContext);
    }

    public boolean prepare(String transactionId) {
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        if (txContextList != null) {
            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    Xid xid = xidRegistry.get(transactionId);
                    if (xaResource != null) {
                        xaResource.prepare(xid);
                    }
                } catch (Throwable e) {
                    log.error("error in prepare the transaction, " + transactionId + ":" + e.getMessage(), e);
                    return false;
                }
            }
        } else {
            log.info("no transacted actions registered for prepare : " + transactionId);
        }
        return true;
    }

    public boolean notifyCommit(String transactionId) {
        boolean commitSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        if (txContextList != null) {
            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    Xid xid = xidRegistry.get(transactionId);
                    if (xaResource != null) {
                        xaResource.commit(xid, false);
                    } else {
                        ctx.commit();
                    }
                } catch (Throwable e) {
                    log.error("error in commit the transaction, " + transactionId + ":" + e.getMessage(), e);
                    commitSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        } else {
            log.info("no transacted actions registered for commit : " + transactionId);
        }
        removeContextsFromRegistry(transactionId);
        return commitSuccess;
    }

    public boolean notifyAbort(String transactionId) {
        boolean abortSuccess = true;
        List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
        if (txContextList != null) {
            for (BallerinaTransactionContext ctx : txContextList) {
                try {
                    XAResource xaResource = ctx.getXAResource();
                    Xid xid = xidRegistry.get(transactionId);
                    if (xaResource != null) {
                        ctx.getXAResource().rollback(xid);
                    } else {
                        ctx.rollback();
                    }
                } catch (Throwable e) {
                    log.error("error in abort the transaction, " + transactionId + ":" + e.getMessage(), e);
                    abortSuccess = false;
                } finally {
                    ctx.close();
                }
            }
        } else {
            log.info("no transacted actions registered for rollback : " + transactionId);
        }
        removeContextsFromRegistry(transactionId);
        return abortSuccess;
    }

    public void removeContextsFromRegistry(String transactionId) {
        resourceRegistry.remove(transactionId);
    }

    public void beginXATransaction(String transactionId, XAResource xaResource) {
        Xid xid = xidRegistry.get(transactionId);
        if (xid == null) {
            xid = XIDGenerator.createXID();
            xidRegistry.put(transactionId, xid);
        }
        try {
            xaResource.start(xid, TMNOFLAGS);
        } catch (XAException e) {
            int i = 0;
        }
    }

    public void endXATransaction(String transactionId) {
        Xid xid = xidRegistry.get(transactionId);
        if (xid != null) {
            List<BallerinaTransactionContext> txContextList = resourceRegistry.get(transactionId);
            if (txContextList != null) {
                for (BallerinaTransactionContext ctx : txContextList) {
                    try {
                        XAResource xaResource = ctx.getXAResource();
                        if (xaResource != null) {
                            ctx.getXAResource().end(xid, TMSUCCESS);
                        } else {
                            ctx.rollback();
                        }
                    } catch (Throwable e) {
                        int i = 0;
                    }
                }
            } else {
                log.info("no transacted actions registered for rollback : " + transactionId);
            }
        }
    }
}
