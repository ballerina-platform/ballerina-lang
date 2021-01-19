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

import org.ballerinalang.debugadapter.evaluation.parser.DebugParser;

/**
 * Debug source validator.
 *
 * @since 2.0.0
 */
public abstract class Validator {

    protected DebugParser debugParser;

    protected Validator(DebugParser parser) {
        this.debugParser = parser;
    }

    /**
     * Validation logic should be implemented using this, by each concrete implementation.
     *
     * @param source ballerina source.
     * @throws Exception if failed due to a validation/parsing error.
     */
    public abstract void validate(String source) throws Exception;

    public DebugParser getDebugParser() {
        return debugParser;
    }

    protected static void failIf(boolean condition, String message) throws ValidatorException {
        if (condition) {
            throw new ValidatorException(message);
        }
    }
}
