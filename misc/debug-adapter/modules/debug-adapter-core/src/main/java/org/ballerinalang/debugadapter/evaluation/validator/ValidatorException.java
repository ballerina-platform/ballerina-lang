/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation.validator;

/**
 * Wrapper to indicate ballerina debug expression validation specific exceptions.
 *
 * @since 2.0.0
 */
public class ValidatorException extends Exception {

    // Invalid input types
    public static final String ERROR_INPUT_EMPTY_EXPRESSION = "Empty expressions cannot be evaluated.";
    public static final String ERROR_INPUT_DOCUMENTATION = "Documentation is not allowed.";
    // Unsupported input types
    public static final String UNSUPPORTED_INPUT_IMPORT = "Import declaration evaluation is not supported.";
    public static final String UNSUPPORTED_INPUT_TOPLEVEL_DCLN = "Top-level declaration evaluation is not supported.";
    public static final String UNSUPPORTED_INPUT_STATEMENT = "Statement evaluation is not supported.";

    public ValidatorException(String message) {
        super(message);
    }
}
