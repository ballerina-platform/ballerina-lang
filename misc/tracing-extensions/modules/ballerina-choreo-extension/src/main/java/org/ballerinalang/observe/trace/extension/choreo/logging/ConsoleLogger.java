/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.logging;

import java.io.PrintStream;

/**
 * Logger that logs everything to console.
 */
public class ConsoleLogger implements Logger {
    private static final PrintStream console = System.out;
    private static final String prefix = "ballerina: ";
    private static final String postfix = "\n";
    private final LogPrinter debugLogPrinter;

    public ConsoleLogger() {
        boolean debugEnabled = Boolean.parseBoolean(System.getenv("CHOREO_TRACING_EXT_DEBUG"));
        if (debugEnabled) {
            debugLogPrinter = new PrintStreamPrinter(console);
        } else {
            debugLogPrinter = new EmptyPrinter();
        }
    }

    @Override
    public void debug(String format, Object... args) {
        debugLogPrinter.printf(prefix + format + postfix, args);
    }

    @Override
    public void info(String format, Object... args) {
        console.printf(prefix + format + postfix, args);
    }

    @Override
    public void error(String format, Object... args) {
        console.printf(prefix + format + postfix, args);
    }

    private interface LogPrinter {
        void printf(String format, Object... args);
    }

    private static class EmptyPrinter implements LogPrinter {
        @Override
        public void printf(String format, Object... args) {
            // do nothing
        }
    }

    private static class PrintStreamPrinter implements LogPrinter {

        private PrintStream printStream;

        private PrintStreamPrinter(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        public void printf(String format, Object... args) {
            printStream.printf(format, args);
        }
    }
}
