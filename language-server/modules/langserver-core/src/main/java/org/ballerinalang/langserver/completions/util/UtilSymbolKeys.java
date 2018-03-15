/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.langserver.completions.util;

/**
 * Utility Symbol Keys related to Completion.
 */
public class UtilSymbolKeys {

    // Start non letter symbol keys
    public static final String OPEN_BRACE_KEY = "{";

    public static final String CLOSE_BRACE_KEY = "}";

    public static final String NOT_FOUND_TYPE = "><";

    public static final String PKG_DELIMITER_KEYWORD = ":";

    public static final String DOT_SYMBOL_KEY = ".";

    public static final String ACTION_INVOCATION_SYMBOL_KEY = "->";
    // End non letter symbol keys


    public static final String ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN = "%params%";

    public static final String CREATE_KEYWORD_KEY = "create";

    public static final String ENDPOINT_KEYWORD_KEY = "endpoint";
}
