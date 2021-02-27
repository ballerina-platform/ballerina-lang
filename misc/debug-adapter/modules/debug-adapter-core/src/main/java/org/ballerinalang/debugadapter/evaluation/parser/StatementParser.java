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

package org.ballerinalang.debugadapter.evaluation.parser;

/**
 * An extended debug parser implementation for ballerina statement-specific parsing.
 *
 * @since 2.0.0
 */
public class StatementParser extends DebugParser {

    public static final String BAL_WRAPPER_FUNCTION_NAME = "wrapper_function";
    public static final String BAL_WRAPPER_FUNCTION_TEMPLATE = "function " + BAL_WRAPPER_FUNCTION_NAME + "() {%s}";

    @Override
    public String preprocess(String source) {
        return String.format(BAL_WRAPPER_FUNCTION_TEMPLATE, source);
    }
}
