/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.internal.types.semtype.MutableSemType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Debug logger for type related operation in the runtime
 *
 * @since 2201.12.0
 */
public class TypeCheckLogger {

    private static final TypeCheckLogger instance = new TypeCheckLogger();
    private final boolean enabled;
    private final Logger logger;

    private TypeCheckLogger() {
        String diagnosticEnable = System.getenv("BAL_LOG_TYPE_CHECK");
        if ("true".equalsIgnoreCase(diagnosticEnable)) {
            enabled = true;
            logger = new Logger(getConfig());
        } else {
            enabled = false;
            logger = null;
        }
    }

    public static TypeCheckLogger getInstance() {
        return instance;
    }

    public void typeCreatedDynamically(Type type) {
        if (enabled) {
            logger.info("Type created dynamically: " + type);
        }
    }

    public void typeResolutionStarted(MutableSemType type) {
        if (enabled) {
            logger.info("Type resolution started: " + type);
        }
    }

    public void typeResolutionDone(MutableSemType type) {
        if (enabled) {
            logger.info("Type resolution done: " + type);
        }
    }

    public void shapeCheckStarted(Object value, Type type) {
        if (enabled) {
            Context cx = TypeChecker.context();
            logger.info("Shape check started: " + cx + ", value: " + value + ", type: " + type);
        }
    }

    public void shapeCheckDone(Object value, Type type, boolean result) {
        if (enabled) {
            Context cx = TypeChecker.context();
            logger.info("Shape check done: " + cx + ", value: " + value + ", type: " + type + ", result: " + result);
        }
    }

    public void typeCheckStarted(Context cx, Type t1, Type t2) {
        if (enabled) {
            logger.info("Type check started: " + cx + ", t1: " + t1 + ", t2: " + t2);
        }
    }

    public void typeCheckCachedResult(Context cx, Type t1, Type t2, Boolean result) {
        if (enabled) {
            logger.info("Type check cached result: " + cx + ", t1: " + t1 + ", t2: " + t2 + ", result: " + result);
        }
    }

    public void semTypeCheckStarted(Context cx, SemType t1, SemType t2) {
        if (enabled) {
            logger.info("SemType check started: " + cx + ", t1: " + t1 + ", t2: " + t2);
        }
    }

    public void semTypeCheckDone(Context cx, SemType t1, SemType t2, boolean result) {
        if (enabled) {
            logger.info("SemType check done: " + cx + ", t1: " + t1 + ", t2: " + t2 + ", result: " + result);
        }
    }

    public void typeCheckDone(Context cx, Type t1, Type t2, boolean result) {
        if (enabled) {
            logger.info("Type check done: " + cx + ", t1: " + t1 + ", t2: " + t2 + ", result: " + result);
        }
    }

    private static LogConfig getConfig() {
        String logPath = System.getenv("BAL_LOG_TYPE_CHECK_PATH");
        boolean isSilent = System.getenv("BAL_LOG_TYPE_CHECK_SILENT") != null;
        if (logPath == null) {
            if (isSilent) {
                throw new IllegalArgumentException(
                        "Type check logging is enabled but set to silent and no log path provided");
            }
            return new LogConfig(Optional.empty(), false);
        }
        return new LogConfig(Optional.of(logPath), isSilent);
    }

    record LogConfig(Optional<String> filePath, boolean isSilent) {

    }

    // Can't use java.util.logger since some library is modifying the global log level at runtime, breaking everything
    private final class Logger {

        private final FileWritter fileWritter;
        private final ConsoleWriter consoleWriter;

        private Logger(LogConfig config) {
            fileWritter = config.filePath().map(FileWritter::new).orElse(null);
            if (!config.isSilent) {
                consoleWriter = new ConsoleWriter();
            } else {
                consoleWriter = null;
            }
        }

        public void info(String message) {
            String formattedMessage = String.format("[INFO] [%d] %s", System.nanoTime(), message);
            writeIfAvailable(fileWritter, formattedMessage);
            writeIfAvailable(consoleWriter, formattedMessage);
        }

        private static void writeIfAvailable(Writer writer, String message) {
            if (writer != null) {
                writer.write(message);
            }
        }

        private interface Writer {

            void write(String message);
        }

        private final class FileWritter implements Writer {

            private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

            private FileWritter(String filePath) {
                Thread thread = new Thread(() -> {
                    // Open file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                        flushToBuffer(writer);
                    } catch (IOException e) {
                        throw new RuntimeException("Log write failed due to: ", e);
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }

            private void flushToBuffer(BufferedWriter writer) throws IOException {
                while (true) {
                    try {
                        String message = queue.take();
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            @Override
            public void write(String message) {
                queue.add(message);
            }
        }

        private static final class ConsoleWriter implements Writer {

            private static final PrintStream outputStream = System.out;

            public void write(String message) {
                outputStream.println(message);
            }
        }
    }
}
