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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.transactions.TransactionConstants.ERROR_MESSAGE_PREFIX;
import static io.ballerina.runtime.transactions.TransactionConstants.NO_CHECKPOINT_INTERVAL;

/**
 * {@code FileRecoveryLog} the file recovery log for transaction recovery.
 *
 * @since 2201.9.0
 */
public final class FileRecoveryLog implements RecoveryLog {

    private static final String LOG_FILE_NUMBER = "(\\d+)";
    private static final String LOG_FILE_EXTENSION = ".log";
    private final String baseFileName;
    private final Path recoveryLogDir;
    private final int checkpointInterval;
    private final boolean deleteOldLogs;
    private int numOfPutsSinceLastCheckpoint;
    private File logFile;
    private FileLockAndChannel fileLockAndChannel;
    private Map<String, TransactionLogRecord> existingLogs;
    private static final PrintStream stderr = System.err;
    private final RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
    private static FileRecoveryLog instance;

    /**
     * Initializes a new FileRecoveryLog instance with the given base file name.
     *
     * @param baseFileName       The base name for the recovery log files.
     * @param checkpointInterval The interval at which to write checkpoints to the log file.
     * @param recoveryLogDir     The directory to store the recovery log files in.
     * @param deleteOldLogs      Whether to delete old log files when creating a new one.
     */
    private FileRecoveryLog(String baseFileName, int checkpointInterval, Path recoveryLogDir, boolean deleteOldLogs) {
        this.baseFileName = baseFileName;
        this.recoveryLogDir = recoveryLogDir;
        this.deleteOldLogs = deleteOldLogs;
        this.checkpointInterval = checkpointInterval;
        this.existingLogs = new HashMap<>();
        this.numOfPutsSinceLastCheckpoint = 0;
    }

    public static FileRecoveryLog getInstance(String baseFileName, int checkpointInterval, Path recoveryLogDir,
                                        boolean deleteOldLogs) {
        if (instance != null) {
           throw new IllegalStateException("instance already exists");
        }
        instance = new FileRecoveryLog(baseFileName, checkpointInterval, recoveryLogDir, deleteOldLogs);
        instance.logFile = instance.createNextVersion();
        return instance;
    }

    /**
     * Creates the next version of the recovery log file, cleaning up the previous one.
     *
     * @return The newly created log file.
     */
    private File createNextVersion() {
        int latestVersion = findLatestVersion();
        File oldFile = recoveryLogDir.resolve(baseFileName + latestVersion + LOG_FILE_EXTENSION).toFile();
        if (oldFile.exists()) {
            existingLogs = readLogsFromFile(oldFile);
            if (deleteOldLogs) {
                File[] matchingLogfiles = getLogFilesInDirectory();
                for (File file : matchingLogfiles) {
                    file.delete();
                }
            }
        }
        File newFile = recoveryLogDir.resolve(baseFileName + (latestVersion + 1) + LOG_FILE_EXTENSION).toFile();
        try {
            Files.createDirectories(recoveryLogDir); // create directory if not exists
            newFile.createNewFile();
            fileLockAndChannel = initAppendChannel(newFile);
            if (existingLogs == null) {
                return newFile;
            }
            // write existing unfinished logs to the new file
            cleanUpFinishedLogs();
            putAll(existingLogs);
            existingLogs.clear();
        } catch (IOException e) {
            stderr.println(ERROR_MESSAGE_PREFIX + " failed to create recovery log file in " + recoveryLogDir + ": "
                    + e.getMessage());
        }
        return newFile;
    }

    /**
     * Finds the latest version of the recovery log file.
     *
     * @return The latest version of the recovery log file.
     */
    private int findLatestVersion() {
        int latestVersion = 0;
        File[] matchingFiles = getLogFilesInDirectory();
        if (matchingFiles == null) {
            return latestVersion;
        }
        for (File file : matchingFiles) {
            String fileName = file.getName();
            int version = Integer.parseInt(
                    fileName.replaceAll(baseFileName, "").replaceAll(LOG_FILE_EXTENSION, ""));
            if (version > latestVersion) {
                latestVersion = version;
            }
        }
        return latestVersion;
    }

    private File[] getLogFilesInDirectory() {
        return recoveryLogDir.toFile().listFiles(
                (dir, name) -> name.matches(baseFileName + LOG_FILE_NUMBER + LOG_FILE_EXTENSION + "$")
        );
    }

    /**
     * Initializes the append channel for the given file.
     *
     * @param file The file to initialize the append channel for.
     */
    private FileLockAndChannel initAppendChannel(File file) {
        if (fileLockAndChannel == null) {
            synchronized (this) {
                if (fileLockAndChannel == null) {
                    try {
                        FileChannel appendChannel = FileChannel.open(file.toPath(), StandardOpenOption.APPEND);
                        FileLock lock = appendChannel.tryLock();
                        if (lock == null) {
                            stderr.println(
                                    ERROR_MESSAGE_PREFIX + " failed to acquire lock on recovery log file "
                                            + file.toPath());
                        } else {
                            fileLockAndChannel = new FileLockAndChannel(lock, appendChannel);
                        }
                    } catch (IOException e) {
                        stderr.println(
                                ERROR_MESSAGE_PREFIX + " failed to acquire lock on recovery log file " + file.toPath() +
                                        ": " + e.getMessage());
                    }
                }
            }
        }
        return fileLockAndChannel;
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        boolean force = !(trxRecord.getTransactionState().equals(RecoveryState.TERMINATED)); // lazy write
        writeToFile(trxRecord.getTransactionLogRecordString(), force);
        if (checkpointInterval != NO_CHECKPOINT_INTERVAL) {
            writeCheckpointIfNeeded();
            numOfPutsSinceLastCheckpoint++;
        }
    }

    @Override
    public void putAll(Map<String, TransactionLogRecord> trxRecords) {
        for (TransactionLogRecord trxRecord : trxRecords.values()) {
            writeToFile(trxRecord.getTransactionLogRecordString(), true);
        }
    }

    public Map<String, TransactionLogRecord> getPendingLogs() {
        Map<String, TransactionLogRecord> pendingTransactions = new HashMap<>();
        Map<String, TransactionLogRecord> transactionLogs = readLogsFromFile(logFile);
        for (Map.Entry<String, TransactionLogRecord> entry : transactionLogs.entrySet()) {
            String trxId = entry.getKey();
            TransactionLogRecord trxRecord = entry.getValue();
            if (!trxRecord.isCompleted()) {
                pendingTransactions.put(trxId, trxRecord);
            }
        }
        return pendingTransactions;
    }

    /**
     * Write a transaction log entry to the recovery log file.
     *
     * @param str the log entry to write
     */
    private void writeToFile(String str, boolean force) {
        if (fileLockAndChannel.appendChannel == null || !fileLockAndChannel.appendChannel.isOpen()) {
            fileLockAndChannel = initAppendChannel(logFile);
        }
        byte[] bytes = str.getBytes();
        try {
            fileLockAndChannel.appendChannel.write(ByteBuffer.wrap(bytes));
            fileLockAndChannel.appendChannel.force(force);
        } catch (IOException e) {
            stderr.println(ERROR_MESSAGE_PREFIX + " failed to write to recovery log file " + logFile.toPath() + ": "
                    + e.getMessage());
        }
    }

    /**
     * Reads the transaction logs from the log file.
     *
     * @param file The file to read the transaction logs from.
     * @return The transaction logs read from the file.
     */
    private Map<String, TransactionLogRecord> readLogsFromFile(File file) {
        if (!file.exists() || file.length() == 0) {
            return Collections.emptyMap();
        }
        if (fileLockAndChannel != null) {
            closeEverything();
        }
        Map<String, TransactionLogRecord> logMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionLogRecord transactionLogRecord = TransactionLogRecord.parseTransactionLogRecord(line);
                if (transactionLogRecord == null) {
                    diagnosticLog.error(ErrorCodes.TRANSACTION_CANNOT_PARSE_LOG_RECORD, null, line);
                    continue;
                }
                logMap.put(transactionLogRecord.getCombinedId(), transactionLogRecord);
            }
        } catch (IOException e) {
            stderr.println(ERROR_MESSAGE_PREFIX + " failed to read the recovery log file " + file.toPath() + ": "
                    + e.getMessage());
        }
        if (!diagnosticLog.getDiagnosticList().isEmpty()) {
            RuntimeUtils.handleDiagnosticErrors(diagnosticLog);
        }
        return logMap;
    }

    private void cleanUpFinishedLogs() {
        if (existingLogs.isEmpty()) {
            return;
        }
        // Safely remove the completed entries
        existingLogs.entrySet().removeIf(entry -> entry.getValue().isCompleted());
    }

    private void writeCheckpointIfNeeded() {
        if (numOfPutsSinceLastCheckpoint >= checkpointInterval) {
            numOfPutsSinceLastCheckpoint = 0; // need to set here otherwise it will just keep creating new files
            logFile = createNextVersion();
        }
    }

    @Override
    public void close() {
        closeEverything();
    }

    private void closeEverything() {
        try {
            fileLockAndChannel.close();
        } catch (IOException e) {
            // nothing to do here.
        }
    }

    public record FileLockAndChannel(FileLock lock, FileChannel appendChannel) {
        public void close() throws IOException {
            if (lock != null) {
                lock.release();
            }
            if (appendChannel != null) {
                appendChannel.close();
            }
        }
    }

}
