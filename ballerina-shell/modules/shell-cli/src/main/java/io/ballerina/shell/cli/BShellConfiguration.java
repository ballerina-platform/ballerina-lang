/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Configuration that uses command utils to provide options.
 */
public class BShellConfiguration {
    private final Evaluator evaluator;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean isDebug;
    private boolean isDumb;

    public BShellConfiguration(boolean isDebug, boolean isDumb) {
        this(isDebug, isDumb, EvaluatorMode.DEFAULT);
    }

    public BShellConfiguration(boolean isDebug, boolean isDumb, EvaluatorMode mode) {
        this(isDebug, isDumb, mode, System.in, System.out);
    }

    public BShellConfiguration(boolean isDebug, EvaluatorMode mode,
                               InputStream inputStream, OutputStream outputStream) {
        this(isDebug, true, mode, inputStream, outputStream);
    }

    private BShellConfiguration(boolean isDebug, boolean isDumb, EvaluatorMode mode,
                                InputStream inputStream, OutputStream outputStream) {
        this.isDebug = isDebug;
        this.isDumb = isDumb;
        this.evaluator = createEvaluator(mode);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Creates and returns an evaluator based on the config.
     *
     * @param mode Mode to create the evaluator on.
     * @return Created evaluator.
     */
    private Evaluator createEvaluator(EvaluatorMode mode) {
        if (mode == EvaluatorMode.DEFAULT) {
            return new EvaluatorBuilder().build();
        }
        throw new RuntimeException("Unknown mode given.");
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

    /**
     * Modes to create the evaluator.
     */
    public enum EvaluatorMode {
        DEFAULT
    }
}
