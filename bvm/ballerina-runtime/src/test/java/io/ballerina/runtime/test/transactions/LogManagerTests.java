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

import io.ballerina.runtime.transactions.FileRecoveryLog;
import io.ballerina.runtime.transactions.InMemoryRecoveryLog;
import io.ballerina.runtime.transactions.LogManager;
import io.ballerina.runtime.transactions.RecoveryState;
import io.ballerina.runtime.transactions.TransactionLogRecord;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LogManagerTests {

    private LogManager logManager;
    private TransactionLogRecord logRecord;
    private Path recoveryLogDir = Path.of("build/tmp/test/recovery/logManagerTestLogs");

    @BeforeSuite
    public void setup() {
        logManager = LogManager.getInstance("testLog", -1, recoveryLogDir, false);
        logRecord = new TransactionLogRecord("00000000-0000-0000-0000-000000000000", "0",
                RecoveryState.PREPARING);
    }

    @Test(description = "Test adding logs records to both in-memory and file recovery logs")
    public void testPut() throws Exception {
        logManager.put(logRecord);
        Field inMemoryRecoveryLogField = LogManager.class.getDeclaredField("inMemoryRecoveryLog");
        inMemoryRecoveryLogField.setAccessible(true);
        InMemoryRecoveryLog inMemoryRecoveryLog = (InMemoryRecoveryLog) inMemoryRecoveryLogField.get(logManager);
        Map<String, TransactionLogRecord> inMemoryLogs = inMemoryRecoveryLog.getAllLogs();
        Field fileRecoveryLogField = LogManager.class.getDeclaredField("fileRecoveryLog");
        fileRecoveryLogField.setAccessible(true);
        FileRecoveryLog fileRecoveryLog = (FileRecoveryLog) fileRecoveryLogField.get(logManager);
        Map<String, TransactionLogRecord> fileLogs = fileRecoveryLog.getPendingLogs();

        Assert.assertTrue(inMemoryLogs.containsKey(logRecord.getCombinedId()));
        Assert.assertTrue(fileLogs.containsKey(logRecord.getCombinedId()));
    }

    @Test(description = "Test getting failed transaction logs from the log manager")
    public void testGetFailedTransactionLogs() {
        logManager.put(logRecord);
        Map<String, TransactionLogRecord> failedTransactions = logManager.getFailedTransactionLogs();
        Assert.assertTrue(failedTransactions.containsValue(logRecord));
    }

    @AfterSuite
    public void tearDown() throws IOException {
        logManager.close();
        if (Files.exists(recoveryLogDir)) {
            Files.walk(recoveryLogDir).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
