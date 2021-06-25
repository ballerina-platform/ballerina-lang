/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli;

import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.parser.TrialTreeParser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Configuration that uses command utils to provide options.
 *
 * @since 2.0.0
 */
public class BShellConfiguration {
    private final Evaluator evaluator;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final String startFile;
    private boolean isDebug;
    private boolean isDumb;

    private BShellConfiguration(boolean isDebug, boolean isDumb, long treeParsingTimeout,
                                InputStream inputStream, OutputStream outputStream, String startFile) {
        this.isDebug = isDebug;
        this.isDumb = isDumb;
        this.evaluator = new EvaluatorBuilder()
                .treeParser(TrialTreeParser.defaultParser(treeParsingTimeout))
                .build();
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.startFile = startFile;
    }

    /**
     * Get the evaluator set by the user.
     *
     * @return Evaluator.
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Whether the configuration is in debug mode.
     *
     * @return Whether in debug mode.
     */
    public boolean isDebug() {
        return isDebug;
    }

    public void toggleDebug() {
        isDebug = !isDebug;
    }

    /**
     * isDumb refers whether the created application is a
     * fully featured one. If isDumb, some features such as auto completion
     * may not be available. This has to be set as true in a testing environment.
     *
     * @return Whether the terminal is forced to be dumb.
     */
    public boolean isDumb() {
        return isDumb;
    }

    public void setDumb(boolean dumb) {
        isDumb = dumb;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Optional<String> getStartFile() {
        return Optional.ofNullable(startFile);
    }

    /**
     * Builder to build the evaluator config.
     *
     * @since 2.0.0
     */
    public static class Builder {
        private InputStream inputStream;
        private OutputStream outputStream;
        private long treeParsingTimeoutMs;
        private String startFile;
        private boolean isDebug;
        private boolean isDumb;

        public Builder() {
            this.inputStream = System.in;
            this.outputStream = System.out;
            this.treeParsingTimeoutMs = 1000;
            this.startFile = null;
            this.isDebug = false;
            this.isDumb = false;
        }

        /**
         * Main input stream to use.
         */
        public Builder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        /**
         * Main output stream to use.
         */
        public Builder setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        /**
         * Tree parsing will be timed out after this.
         * This value is set so that the syntax tree parsing will not
         * take a huge amount of time.
         * Set to a higher value in low spec devices.
         */
        public Builder setTreeParsingTimeoutMs(long treeParsingTimeoutMs) {
            this.treeParsingTimeoutMs = treeParsingTimeoutMs;
            return this;
        }

        /**
         * Debug mode will enable performance and similar stats.
         * These will also enable debug messages.
         */
        public Builder setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        /**
         * Dumb terminals are terminals that do not support ANSI or similar.
         * These terminals will not support auto completion or similar features.
         * Colors will also be disabled in dumb terminals.
         */
        public Builder setDumb(boolean dumb) {
            isDumb = dumb;
            return this;
        }

        /**
         * Start file is the file containing declarations that should
         * be loaded into the shell initially.
         * If the file loading fails, the shell will not start and fail.
         * The file should not contain any definitions for built-in names.
         * (eg: main, run, init)
         */
        public Builder setStartFile(String startFile) {
            this.startFile = startFile;
            return this;
        }

        /**
         * Builds a configuration for ballerina shell.
         *
         * @return Created ballerina shell config.
         */
        public BShellConfiguration build() {
            return new BShellConfiguration(isDebug, isDumb, treeParsingTimeoutMs,
                    inputStream, outputStream, startFile);
        }
    }
}
