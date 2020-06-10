/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package io.ballerina.transactions;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.transactions.TransactionConstants;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.transactions.TransactionResourceManager;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static org.ballerinalang.jvm.runtime.RuntimeConstants.GLOBAL_TRANSACTION_ID;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.TRANSACTION_URL;
import static org.ballerinalang.jvm.transactions.TransactionConstants.TRANSACTION_PACKAGE_ID;

/**
 * Native function implementations of the transactions module.
 *
 * @since 1.1.0
 */
public class Utils {
    private static final String STRUCT_TYPE_TRANSACTION_CONTEXT = "TransactionContext";

    public static void notifyResourceManagerOnAbort(BString transactionBlockId) {
        Strand strand = Scheduler.getStrand();
        org.ballerinalang.jvm.transactions.TransactionLocalContext transactionLocalContext =
                strand.transactionLocalContext;
        org.ballerinalang.jvm.transactions.TransactionResourceManager.getInstance()
                .notifyAbort(transactionLocalContext.getGlobalTransactionId(), transactionBlockId.getValue());
    }

    public static void rollbackTransaction(BString transactionBlockId) {
        Strand strand = Scheduler.getStrand();
        TransactionLocalContext transactionLocalContext = strand.transactionLocalContext;
        transactionLocalContext.rollbackTransaction(transactionBlockId.getValue());
    }

    public static void cleanupTransactionContext(BString transactionBlockId) {
        Strand strand = Scheduler.getStrand();
        strand.removeLocalTransactionContext();
    }

    public static boolean getAndClearFailure() {
        Strand strand = Scheduler.getStrand();
        return strand.transactionLocalContext.getAndClearFailure() != null;
    }

    public static void notifyRemoteParticipantOnFailure() {
        Strand strand = Scheduler.getStrand();
        org.ballerinalang.jvm.transactions.TransactionLocalContext transactionLocalContext =
                strand.transactionLocalContext;
        if (transactionLocalContext == null) {
            return;
        }
        transactionLocalContext.notifyLocalRemoteParticipantFailure();
    }

    public static void notifyLocalParticipantOnFailure() {
        Strand strand = Scheduler.getStrand();
        org.ballerinalang.jvm.transactions.TransactionLocalContext transactionLocalContext =
                strand.transactionLocalContext;
        if (transactionLocalContext == null) {
            return;
        }
        transactionLocalContext.notifyLocalParticipantFailure();
    }

    public static Object registerRemoteParticipant(BString transactionBlockId, FPValue fpCommitted, FPValue fpAborted) {
        Strand strand = Scheduler.getStrand();
        String gTransactionId = (String) strand.getProperty(GLOBAL_TRANSACTION_ID);
        if (gTransactionId == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return null;
        }

        // Create transaction context and store in the strand.
        TransactionLocalContext transactionLocalContext = TransactionLocalContext
                .create(gTransactionId, strand.getProperty(TRANSACTION_URL).toString(), "2pc");
        strand.transactionLocalContext = transactionLocalContext;

        // Register committed and aborted function handler if exists.
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        transactionResourceManager.registerParticipation(transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId.getValue(), fpCommitted, fpAborted, strand);
        MapValue<BString, Object> trxContext = BallerinaValues.createRecordValue(TRANSACTION_PACKAGE_ID,
                                                                                 STRUCT_TYPE_TRANSACTION_CONTEXT);
        Object[] trxContextData = new Object[] {
                TransactionConstants.DEFAULT_CONTEXT_VERSION, transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId.getValue(), transactionLocalContext.getProtocol(), transactionLocalContext.getURL()
        };
        return BallerinaValues.createRecord(trxContext, trxContextData);
    }

    public static Object registerLocalParticipant(BString transactionBlockId, FPValue fpCommitted, FPValue fpAborted) {
        Strand strand = Scheduler.getStrand();
        TransactionLocalContext transactionLocalContext = strand.transactionLocalContext;
        if (transactionLocalContext == null) {
            // No transaction available to participate,
            // We have no business here. This is a no-op.
            return null;
        }
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();

        // Register committed and aborted function handler if exists.
        transactionResourceManager.registerParticipation(transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId.getValue(), fpCommitted, fpAborted, strand);
        MapValue<BString, Object> trxContext = BallerinaValues.createRecordValue(TRANSACTION_PACKAGE_ID,
                STRUCT_TYPE_TRANSACTION_CONTEXT);
        Object[] trxContextData = new Object[] {
                TransactionConstants.DEFAULT_CONTEXT_VERSION, transactionLocalContext.getGlobalTransactionId(),
                transactionBlockId.getValue(), transactionLocalContext.getProtocol(), transactionLocalContext.getURL()
        };
        return BallerinaValues.createRecord(trxContext, trxContextData);
    }

    public static void setTransactionContext(MapValue txDataStruct) {
        Strand strand = Scheduler.getStrand();
        String globalTransactionId = txDataStruct.get(TransactionConstants.TRANSACTION_ID).toString();
        String transactionBlockId = txDataStruct.get(TransactionConstants.TRANSACTION_BLOCK_ID).toString();
        String url = txDataStruct.get(TransactionConstants.REGISTER_AT_URL).toString();
        String protocol = txDataStruct.get(TransactionConstants.CORDINATION_TYPE).toString();
        TransactionLocalContext trxCtx = TransactionLocalContext
                .createTransactionParticipantLocalCtx(globalTransactionId, url, protocol);
        trxCtx.beginTransactionBlock(transactionBlockId);
        strand.transactionLocalContext = trxCtx;
    }

    public static boolean isNestedTransaction() {
        Strand strand = Scheduler.getStrand();
        return strand.transactionLocalContext != null;
    }

    public static BString getCurrentTransactionId() {
        Strand strand = Scheduler.getStrand();
        String currentTransactionId = "";
        TransactionLocalContext transactionLocalContext = strand.transactionLocalContext;
        if (transactionLocalContext != null) {
            currentTransactionId = transactionLocalContext.getGlobalTransactionId() + ":" + transactionLocalContext
                    .getCurrentTransactionBlockId();
        }
        return StringUtils.fromString(currentTransactionId);
    }

    public static boolean abortResourceManagers(BString transactionId, BString transactionBlockId) {
        return TransactionResourceManager.getInstance().notifyAbort(transactionId.getValue(),
                                                                    transactionBlockId.getValue());
    }

    public static boolean commitResourceManagers(BString transactionId, BString transactionBlockId) {
        Strand strand = Scheduler.getStrand();
        return org.ballerinalang.jvm.transactions.TransactionResourceManager
                .getInstance().notifyCommit(strand, transactionId.getValue(), transactionBlockId.getValue());
    }

    public static boolean prepareResourceManagers(BString transactionId, BString transactionBlockId) {
        return TransactionResourceManager.getInstance().prepare(transactionId.getValue(),
                                                                transactionBlockId.getValue());
    }

    public static long getAvailablePort() {
        return findFreePort();
    }

    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException ignored) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port");
    }

    public static BString getHostAddress() {
        return StringUtils.fromString(getLocalHostLANAddress().getHostAddress());
    }

    private static InetAddress getLocalHostLANAddress() throws RuntimeException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine LAN address: " + e, e);
        }
    }
}
