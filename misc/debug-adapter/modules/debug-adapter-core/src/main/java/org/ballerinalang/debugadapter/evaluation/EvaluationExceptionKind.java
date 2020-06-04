/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.ballerinalang.debugadapter.evaluation;

/**
 * Ballerina expression evaluation related exception kinds.
 *
 * @since 2.0.0
 */
public enum EvaluationExceptionKind {

    BLOCK_EVALUATION("Block expressions/statements are not supported"),
    EMPTY("Empty expressions cannot be evaluated."),
    INVALID("Invalid expression: \"%s\""),
    SYNTAX_ERROR("Syntax errors found: " + System.lineSeparator() + "%s"),
    UNSUPPORTED("Unsupported expressions/sub-expressions found: " + System.lineSeparator() + "%s");

    public static final String PREFIX = "Failed to evaluate." + System.lineSeparator() + "Reason: ";
    private final String value;

    EvaluationExceptionKind(String value) {
        this.value = value;
    }

    public String getString() {
        return PREFIX + this.value;
    }
}
