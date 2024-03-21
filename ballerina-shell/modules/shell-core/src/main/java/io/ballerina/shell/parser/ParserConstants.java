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

package io.ballerina.shell.parser;

import java.util.Set;

/**
 * Class contains constant values related to shell parser trials.
 *
 * @since 2.0.0
 */
public class ParserConstants {

    public static final String WRAPPER_PREFIX = "__shell_wrapper__";

    public static final Set<String> RESTRICTED_FUNCTION_NAMES = Set.of("main", "init", "__java_recall",
            "__java_memorize", "__recall_any", "__recall_any_error", "__memorize", "__stmts", "__run");

    /**
     * Checks if the given function name is restricted.
     *
     * @param functionName function name to check
     * @return <code>true</code> if the function name is restricted. <code>false</code> otherwise
     */
    public static boolean isFunctionNameRestricted(String functionName) {
        return RESTRICTED_FUNCTION_NAMES.contains(functionName) || functionName.startsWith(WRAPPER_PREFIX);
    }

    private ParserConstants() {}
}
