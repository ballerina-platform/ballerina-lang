package io.ballerina.runtime.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class FileRecoveryLog {
    private static final Logger log = LoggerFactory.getLogger(FileRecoveryLog.class);
    private String baseFileName;
    private File file;
    private FileChannel appendChannel = null;

    private Map<Integer, String> existingLogs;

    public FileRecoveryLog(String baseFileName) {
        this.existingLogs = new HashMap<>();
        this.baseFileName = baseFileName;
        this.file = createNextVersion();

    }

    private File createNextVersion() {
        int latestVersion = findLatestVersion();
        String newFileName = baseFileName + (latestVersion + 1) + ".log";

        // Delete the old version, if it exists
        File oldFile = new File(baseFileName + latestVersion + ".log");
        if (oldFile.exists()) {
            try {
                existingLogs = readLogsFromFile(oldFile);
            } catch (IOException e) {
                log.error("An error occurred while reading the existing recovery log file.");
            }
            oldFile.delete();
        }

        // Create the new version
        File newFile = new File(newFileName);
        try {
            newFile.createNewFile();
            initAppendChannel(newFile);
            if (!existingLogs.isEmpty()){
                // call cleanUpFinishedLogs() here
                for (Map.Entry<Integer, String> entry : existingLogs.entrySet()) {
                    writeToFile(entry.getValue() + "\n");
                }

            }
        } catch (IOException e) {
            log.error("An error occurred while creating the new recovery log file.");
        }
        return newFile;
    }

    private int findLatestVersion() {
        int latestVersion = 0;
        File directory = new File(".");
        File[] files = directory.listFiles((dir, name) -> name.matches(baseFileName + "(\\d+)\\.log"));
        for (File file : files) {
            String fileName = file.getName();
            int version = Integer.parseInt(fileName.replaceAll(baseFileName, "").replaceAll(".log", ""));
            if (version > latestVersion) {
                latestVersion = version;
            }
        }
        return latestVersion;
    }

    public void put(String str) {
        // TODO: add logic to build the log string from the transaction
        writeToFile(str);
    }

    private void initAppendChannel(File file) {
        try {
            appendChannel = FileChannel.open(file.toPath(), StandardOpenOption.APPEND);

            FileLock lock = appendChannel.tryLock();
            if (lock == null) {
                log.error("Could not acquire lock on recovery log file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeToFile(String str) {
        byte[] bytes = str.getBytes();
        try {
            appendChannel.write(java.nio.ByteBuffer.wrap(bytes));
            log.info(String.format("Wrote to recovery log file: " + str));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, String> readLogsFromFile(File file) throws IOException {
        Map logMap = new HashMap<>();
        FileChannel readChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        try {
            FileLock lock = readChannel.tryLock(0, Long.MAX_VALUE, true);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder lineBuilder = new StringBuilder();
            while (readChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    char c = (char) buffer.get();
                    if (c == '\n') {
                        logMap.put(logMap.size() + 1, lineBuilder.toString());
                        lineBuilder.setLength(0);
                    } else {
                        lineBuilder.append(c);
                    }
                }
                buffer.clear();
            }
            // Handle the last line if it doesn't end with a newline
            if (lineBuilder.length() > 0) {
                logMap.put(logMap.size() + 1, lineBuilder.toString());
            }
            lock.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        readChannel.close();
        return logMap;
    }

    private void cleanUpFinishedLogs(){
        // TODO: logic to clean up completed transaction logs

    }

    private void writeCheckpoint() {
        // TODO:  to write checkpoint
    }

    private void closeEverything() {
        try {
            appendChannel.close();
        } catch (IOException e) {
            // nothing to do. java cray cray. :)
        }
    }
}
