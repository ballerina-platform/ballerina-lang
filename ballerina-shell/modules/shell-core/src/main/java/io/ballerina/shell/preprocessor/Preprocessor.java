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

package io.ballerina.shell.preprocessor;

import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.PreprocessorException;

import java.util.Collection;

/**
 * Preprocessor is the first transformational phase of the
 * program. Any input is sent through the preprocessor to convert
 * the input into a list of individually processable statements.
 * For an example any multiple statement input will be split into the
 * relevant list of string counterpart at the end of this phase.
 * The implemented `SeparatorPreprocessor` currently splits the statements
 * into separated lists depending on the semicolons that are in th root bracket level.
 * The motivation of a preprocessor is to divide the input into separately
 * identifiable sections so each can be individually processed on.
 *
 * @since 2.0.0
 */
public abstract class Preprocessor extends DiagnosticReporter {
    /**
     * Preprocesses the string and output the list of
     * processed outputs.
     *
     * @param input Input string
     * @return Processed resultant strings
     * @throws PreprocessorException If the preprocessing failed.
     */
    public abstract Collection<String> process(String input) throws PreprocessorException;
}
