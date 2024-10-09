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

package io.ballerina.runtime.test.transactions;

import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnostic;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.transactions.LogManager;
import io.ballerina.runtime.transactions.RecoveryManager;
import io.ballerina.runtime.transactions.RecoveryState;
import io.ballerina.runtime.transactions.TransactionLogRecord;
import io.ballerina.runtime.transactions.TransactionResourceManager;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class RecoveryManagerTests {
    private RecoveryManager recoveryManager;
    private XAResource xaResource;
    private Map<String, TransactionLogRecord> transactionLogRecords;

    @BeforeMethod
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TransactionResourceManager transactionResourceManager = TransactionResourceManager.getInstance();
        recoveryManager = new RecoveryManager();
        Field logManagerField = TransactionResourceManager.class.getDeclaredField("logManager");
        logManagerField.setAccessible(true);
        LogManager logManager = LogManager.getInstance("testLog", -1,
                Path.of("build/tmp/test/recovery/testRecoveryLogs"), true);
        logManagerField.set(transactionResourceManager, logManager);

        Field field = TransactionResourceManager.class.getDeclaredField("recoveryManager");
        field.setAccessible(true);
        field.set(transactionResourceManager, recoveryManager);
        xaResource = Mockito.mock(XAResource.class);
        transactionLogRecords = new HashMap<>();
        TransactionLogRecord logRecord = Mockito.mock(TransactionLogRecord.class);
        transactionLogRecords.put("testTransaction", logRecord);
    }

    @Test (description = "Test adding XA resource to be recovered to the recovery manager")
    public void testAddXAResourceToRecover() throws NoSuchFieldException, IllegalAccessException {
        recoveryManager.addXAResourceToRecover(xaResource);
        Field field = RecoveryManager.class.getDeclaredField("xaResources");
        field.setAccessible(true);
        Collection<XAResource> xaResources = (Collection<XAResource>) field.get(recoveryManager);
        Assert.assertTrue(xaResources.contains(xaResource));
    }

    @Test (description = "Test adding failed transaction records to the recovery manager")
    public void testRetrievingTransactionsToRecover()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, TransactionLogRecord> transactionLogRecords = new HashMap<>();
        TransactionLogRecord logRecord = Mockito.mock(TransactionLogRecord.class);
        transactionLogRecords.put("testTransaction", logRecord);
        Method method = RecoveryManager.class.getDeclaredMethod("putFailedTransactionRecords", Map.class);
        method.setAccessible(true);
        method.invoke(recoveryManager, transactionLogRecords);

        Map<String, TransactionLogRecord> pendingRecords = recoveryManager.getPendingTransactionRecords();
        Assert.assertEquals(pendingRecords, transactionLogRecords);
    }

    @Test (description = "Test recovery pass when there are no failed transactions")
    public void testRecoveryPassWithNoFailedTransactions() throws NoSuchFieldException, IllegalAccessException {
        recoveryManager.addXAResourceToRecover(xaResource);
        Map<String, TransactionLogRecord> failedTransactions = new HashMap<>();
        Field failedTransactionsField = RecoveryManager.class.getDeclaredField("failedTransactions");
        failedTransactionsField.setAccessible(true);
        failedTransactionsField.set(recoveryManager, failedTransactions);

        Assert.assertTrue(recoveryManager.performRecoveryPass());
    }

    @Test (description = "Test startup recovery pass recovering from all recoverable states")
    public void testRecoverFromRecoverableStates() throws NoSuchFieldException, IllegalAccessException {
        recoveryManager.addXAResourceToRecover(xaResource);

        LogManager logManager = Mockito.mock(LogManager.class);
        Map<String, TransactionLogRecord> failedTransactions = new HashMap<>();
        List<RecoveryState> states = Arrays.asList(RecoveryState.PREPARING, RecoveryState.COMMITTING,
                RecoveryState.ABORTING, RecoveryState.COMMITTED, RecoveryState.ABORTED);
        for (RecoveryState state : states) {
            String trxId = "trxId" + state.name();
            String trxBlockId = "trxBlockId" + state.name();
            TransactionLogRecord record = Mockito.mock(TransactionLogRecord.class);
            Mockito.when(record.getTransactionId()).thenReturn(trxId);
            Mockito.when(record.getTransactionBlockId()).thenReturn(trxBlockId);
            Mockito.when(record.getTransactionState()).thenReturn(state);
            Mockito.when(record.getCombinedId()).thenReturn(trxId + ":" + trxBlockId);
            failedTransactions.put(record.getCombinedId(), record);
        }
        Mockito.when(logManager.getFailedTransactionLogs()).thenReturn(failedTransactions);
        Field failedTransactionsField = RecoveryManager.class.getDeclaredField("failedTransactions");
        failedTransactionsField.setAccessible(true);
        failedTransactionsField.set(recoveryManager, failedTransactions);

        recoveryManager.performRecoveryPass(); // perform recovery pass

        Map<String, TransactionLogRecord> failedTransactionAfterRecovery =
                (Map<String, TransactionLogRecord>) failedTransactionsField.get(recoveryManager);
        Assert.assertTrue(failedTransactionAfterRecovery.isEmpty()); // means recovery was successful
    }

    @Test (description = "Test startup recovery pass recovering from hazard and mixed states")
    public void testRecoveryForHazardMixedStates() throws NoSuchFieldException, IllegalAccessException {
        recoveryManager.addXAResourceToRecover(xaResource);

        LogManager logManager = Mockito.mock(LogManager.class);
        Map<String, TransactionLogRecord> failedTransactions = new HashMap<>();

        List<RecoveryState> states = Arrays.asList(RecoveryState.HAZARD, RecoveryState.MIXED);

        for (RecoveryState state : states) {
            String trxId = "trxId" + state.name();
            String trxBlockId = "trxBlockId" + state.name();
            TransactionLogRecord record = Mockito.mock(TransactionLogRecord.class);
            Mockito.when(record.getTransactionId()).thenReturn(trxId);
            Mockito.when(record.getTransactionBlockId()).thenReturn(trxBlockId);
            Mockito.when(record.getTransactionState()).thenReturn(state);
            Mockito.when(record.getCombinedId()).thenReturn(trxId + ":" + trxBlockId);
            failedTransactions.put(record.getCombinedId(), record);
        }

        Mockito.when(logManager.getFailedTransactionLogs()).thenReturn(failedTransactions);
        Field failedTransactionsField = RecoveryManager.class.getDeclaredField("failedTransactions");
        failedTransactionsField.setAccessible(true);
        failedTransactionsField.set(recoveryManager, failedTransactions);

        Field diagnosticLogsField = RecoveryManager.class.getDeclaredField("diagnosticLog");
        diagnosticLogsField.setAccessible(true);

        recoveryManager.performRecoveryPass();

        Map<String, TransactionLogRecord> failedTransactionAfterRecovery =
                (Map<String, TransactionLogRecord>) failedTransactionsField.get(recoveryManager);
        RuntimeDiagnosticLog diagnosticLogs = (RuntimeDiagnosticLog) diagnosticLogsField.get(recoveryManager);
        List<RuntimeDiagnostic> diagnosticList = diagnosticLogs.getDiagnosticList();

        Assert.assertEquals(diagnosticList.size(), 2);
        Assert.assertTrue(failedTransactionAfterRecovery.isEmpty()); // means user was warned and recovery successful
    }
    
    @Test (description = "Test handling recovery when decision was commit and resource has heur committed")
    public void testHeuristicTerminationWhenDecisionCommitWithHeurCommit()
            throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Xid mockXid = Mockito.mock(Xid.class);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURCOM;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class,
                        boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        // Invoke the method with a decisionCommit value of true
        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(recoveryManager, mockXid,
                mockXaResource, mockXaException, true);
        Assert.assertTrue(result);
    }

    @Test (description = "Test handling recovery when decision was commit and resource has heur rolledback")
    public void testHeuristicTerminationWhenDecisionCommitWithHeurRollback()
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {

        Xid mockXid = Mockito.mock(Xid.class);
        Mockito.when(mockXid.getGlobalTransactionId()).thenReturn(new byte[0]);
        Mockito.when(mockXid.getBranchQualifier()).thenReturn(new byte[0]);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURRB;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class, boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        // Invoke with a decisionCommit value of true
        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(
                recoveryManager, mockXid, mockXaResource, mockXaException, true);

        Field diagnosticLogsField = RecoveryManager.class.getDeclaredField("diagnosticLog");
        diagnosticLogsField.setAccessible(true);
        RuntimeDiagnosticLog diagnosticLogs = (RuntimeDiagnosticLog) diagnosticLogsField.get(recoveryManager);
        List<RuntimeDiagnostic> diagnosticList = diagnosticLogs.getDiagnosticList();

        Assert.assertTrue(result);
        Assert.assertEquals(diagnosticList.size(), 1);
        Assert.assertEquals(diagnosticList.get(0).diagnosticInfo().code(),
                ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE.diagnosticId());
    }

    @Test (description = "Test handling recovery when decision was rollback and resource has heur rolledback")
    public void testHeuristicTerminationWhenDecisionRollbackWithHeurRollback()
            throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Xid mockXid = Mockito.mock(Xid.class);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURRB;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class, boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        // Invoke the method with a decisionCommit value of false
        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(recoveryManager, mockXid,
                mockXaResource, mockXaException, false);
        Assert.assertTrue(result);
    }

    @Test (description = "Test handling recovery when decision was rollback and resource has heur committed")
    public void testHeuristicTerminationWhenDecisionRollbackWithHeurCommit()
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {

        Xid mockXid = Mockito.mock(Xid.class);
        Mockito.when(mockXid.getGlobalTransactionId()).thenReturn(new byte[0]);
        Mockito.when(mockXid.getBranchQualifier()).thenReturn(new byte[0]);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURCOM;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class, boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        // Invoke with a decisionCommit value of false
        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(recoveryManager, mockXid,
                mockXaResource, mockXaException, false);

        Field diagnosticLogsField = RecoveryManager.class.getDeclaredField("diagnosticLog");
        diagnosticLogsField.setAccessible(true);
        RuntimeDiagnosticLog diagnosticLogs = (RuntimeDiagnosticLog) diagnosticLogsField.get(recoveryManager);
        List<RuntimeDiagnostic> diagnosticList = diagnosticLogs.getDiagnosticList();

        Assert.assertTrue(result);
        Assert.assertEquals(diagnosticList.size(), 1);
        Assert.assertEquals(diagnosticList.get(0).diagnosticInfo().code(),
                ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE.diagnosticId());
    }

    @Test (description = "Test handling recovery when a resource is in mixed state")
    public void testHeuristicTerminationWithHuerMixed()
            throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Xid mockXid = Mockito.mock(Xid.class);
        Mockito.when(mockXid.getGlobalTransactionId()).thenReturn(new byte[0]);
        Mockito.when(mockXid.getBranchQualifier()).thenReturn(new byte[0]);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURMIX;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class, boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(recoveryManager, mockXid,
                mockXaResource, mockXaException, true);

        Field diagnosticLogsField = RecoveryManager.class.getDeclaredField("diagnosticLog");
        diagnosticLogsField.setAccessible(true);
        RuntimeDiagnosticLog diagnosticLogs = (RuntimeDiagnosticLog) diagnosticLogsField.get(recoveryManager);
        List<RuntimeDiagnostic> diagnosticList = diagnosticLogs.getDiagnosticList();

        Assert.assertTrue(result);
        Assert.assertEquals(diagnosticList.size(), 1);
        Assert.assertEquals(diagnosticList.get(0).diagnosticInfo().code(),
                ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE.diagnosticId());
    }

    @Test (description = "Test handling recovery when a resource is in heur hazard")
    public void testHeuristicTerminationWithHuerHazard()
            throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Xid mockXid = Mockito.mock(Xid.class);
        Mockito.when(mockXid.getGlobalTransactionId()).thenReturn(new byte[0]);
        Mockito.when(mockXid.getBranchQualifier()).thenReturn(new byte[0]);
        XAResource mockXaResource = Mockito.mock(XAResource.class);
        XAException mockXaException = Mockito.mock(XAException.class);

        mockXaException.errorCode = XAException.XA_HEURHAZ;

        Method handleHeuristicTerminationMethod = RecoveryManager.class.getDeclaredMethod(
                "handleHeuristicTermination", Xid.class, XAResource.class, XAException.class, boolean.class);
        handleHeuristicTerminationMethod.setAccessible(true);

        boolean result = (boolean) handleHeuristicTerminationMethod.invoke(recoveryManager, mockXid,
                mockXaResource, mockXaException, true);

        Field diagnosticLogsField = RecoveryManager.class.getDeclaredField("diagnosticLog");
        diagnosticLogsField.setAccessible(true);
        RuntimeDiagnosticLog diagnosticLogs = (RuntimeDiagnosticLog) diagnosticLogsField.get(recoveryManager);
        List<RuntimeDiagnostic> diagnosticList = diagnosticLogs.getDiagnosticList();

        Assert.assertFalse(result);
        Assert.assertEquals(diagnosticList.size(), 1);
        Assert.assertEquals(diagnosticList.get(0).diagnosticInfo().code(),
                ErrorCodes.TRANSACTION_IN_HUERISTIC_STATE.diagnosticId());
    }

    @Test (description = "Test unable to create file")


    @AfterTest
    public void tearDown() throws IOException {
        Path dir = Paths.get("build/tmp/test/recovery/testRecoveryLogs");
        if (Files.exists(dir)) {
            Files.walk(dir).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
