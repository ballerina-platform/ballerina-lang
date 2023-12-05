package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileRecoveryLog implements RecoveryLog {
    private static final Logger log = LoggerFactory.getLogger(FileRecoveryLog.class);
    private String baseFileName;
    private Path recoveryLogDir;
    private int checkpointInterval;
    private final boolean deleteOldLogs;
    private int numOfPutsSinceLastCheckpoint;
    private File file;
    private FileChannel appendChannel = null;
    private Map<String, TransactionLogRecord> existingLogs;

    /**
     * Initializes a new FileRecoveryLog instance with the given base file name.
     *
     * @param baseFileName  The base name for the recovery log files.
     * @param checkpointInterval The interval at which to write checkpoints to the log file.
     * @param recoveryLogDir The directory to store the recovery log files in.
     * @param deleteOldLogs Whether to delete old log files when creating a new one.
     */
    public FileRecoveryLog(String baseFileName, int checkpointInterval, Path recoveryLogDir, boolean deleteOldLogs) {
        this.baseFileName = baseFileName;
        this.recoveryLogDir = recoveryLogDir;
        this.deleteOldLogs = deleteOldLogs;
        this.checkpointInterval = checkpointInterval;
        this.existingLogs = new HashMap<>();
        this.file = createNextVersion();
        this.numOfPutsSinceLastCheckpoint = 0;
    }

    /**
     * Creates the next version of the recovery log file, cleaning up the previous one.
     *
     * @return The newly created log file.
     */
    private File createNextVersion() {
        int latestVersion = findLatestVersion();
        File oldFile = recoveryLogDir.resolve(baseFileName + latestVersion + ".log").toFile();
        if (oldFile.exists()) {
            existingLogs = readLogsFromFile(oldFile);
            if (deleteOldLogs) {
                File[] files = recoveryLogDir.toFile().listFiles(
                        (dir, name) -> name.matches(baseFileName + "(\\d+)\\.log")
                );
                for (File file: files) {
                        file.delete();
                    }
                }
            }
        File newFile = recoveryLogDir.resolve(baseFileName + (latestVersion + 1) + ".log").toFile();
        try {
            Files.createDirectories(recoveryLogDir); // create directory if not exists
            newFile.createNewFile();
            initAppendChannel(newFile);
            if (existingLogs == null) {
                return newFile;
            }
            // write existing unfinished logs to the new file
            if (!existingLogs.isEmpty()) {
                cleanUpFinishedLogs();
                if (!existingLogs.isEmpty()) {
                    for (Map.Entry<String, TransactionLogRecord> entry : existingLogs.entrySet()) {
                        put(entry.getValue());
                    }
                    existingLogs.clear();
                }
            }
        } catch (IOException e) {
            log.error("An error occurred while creating the new recovery log file.");
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
        File directory = recoveryLogDir.toFile();
        File[] files = directory.listFiles((dir, name) -> name.matches(baseFileName + "(\\d+)\\.log"));
        if (files == null) {
            return latestVersion;
        }
        for (File file : files) {
            String fileName = file.getName();
            int version = Integer.parseInt(fileName.replaceAll(
                    baseFileName, "").replaceAll(".log", "")
            );
            if (version > latestVersion) {
                latestVersion = version;
            }
        }
        return latestVersion;
    }

    /**
     * Initializes the append channel for the given file.
     *
     * @param file The file to initialize the append channel for.
     */
    private void initAppendChannel(File file) {
        try {
            appendChannel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            FileLock lock = appendChannel.tryLock();
            if (lock == null) {
                log.error("Could not acquire lock on recovery log file.");
            }
        } catch (IOException e) {
            log.error("An error occurred while opening the recovery log file.");
        }
    }

    @Override
    public void put(TransactionLogRecord trxRecord) {
        boolean force = !trxRecord.getTransactionStatus().equals(RecoveryStatus.TERMINATED); // lazy write for done record
        writeToFile(trxRecord.getTransactionLogRecord(), force);
        if (checkpointInterval != -1) {
            ifNeedWriteCheckpoint();
            numOfPutsSinceLastCheckpoint++;
        }
    }

    public Map<String, TransactionLogRecord> getPendingLogs() {
        Map<String, TransactionLogRecord> pendingTransactions = new HashMap<>();
        Map<String, TransactionLogRecord> transactionLogs = readLogsFromFile(file);
        if (transactionLogs == null) {
            return null;
        }
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
        if (appendChannel == null || !appendChannel.isOpen()) {
            initAppendChannel(file);
        }
        byte[] bytes = str.getBytes();
        try {
            appendChannel.write(java.nio.ByteBuffer.wrap(bytes));
            appendChannel.force(force);
            log.debug(String.format("Wrote to recovery log file: " + str));
        } catch (IOException e) {
            log.error("An error occurred while writing to the recovery log file.");
        }
    }


    private Map<String, TransactionLogRecord> readLogsFromFile(File file) {
        Map<String, TransactionLogRecord> logMap = new HashMap<>();
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        if (appendChannel != null) {
            closeEverything();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionLogRecord transactionLogRecord = TransactionLogRecord.parseTransactionLogRecord(line);
                if (transactionLogRecord != null) {
                    // TODO: add a check here to check whether the record exists in the logMap and new state is a valid
                    //  state from the current state
                    logMap.put(transactionLogRecord.transactionId, transactionLogRecord);
                }
            }
        } catch (IOException e) {
            log.error("An error occurred while reading the recovery log file.", e);
        }
        return logMap;
    }


    private void cleanUpFinishedLogs() {
        if (existingLogs.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, TransactionLogRecord>> iterator = existingLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TransactionLogRecord> entry = iterator.next();
            if (entry.getValue().isCompleted()) {
                iterator.remove(); // Safely remove the entry
            }
        }
    }

    public void ifNeedWriteCheckpoint() {
        if (numOfPutsSinceLastCheckpoint >= checkpointInterval) {
            log.info("Checkpoint needed. Cleaning up finished logs.");
            numOfPutsSinceLastCheckpoint = 0; // need to set here otherwise it will just keep creating new files
            File newFile = createNextVersion();
            file = newFile;
        }
    }

    public void cleanUpAfterRecovery() {
        int latestVersion = findLatestVersion();
        File oldFile = recoveryLogDir.resolve(baseFileName + latestVersion + ".log").toFile();
        if (oldFile.exists()) {
               oldFile.delete();
            }

        File newFile = recoveryLogDir.resolve(baseFileName + (latestVersion + 1) + ".log").toFile();
        try {
            Files.createDirectories(recoveryLogDir); // create directory if not exists
            newFile.createNewFile();
            initAppendChannel(newFile);
        } catch (IOException e) {
            log.error("An error occurred while creating the new recovery log file.");
        }
        file = newFile;
    }

    @Override
    public void close() {
    }

    private void closeEverything() {
        try {
            appendChannel.close();
        } catch (IOException e) {
            // nothing to do. java cray cray. :)
        }
    }
}
