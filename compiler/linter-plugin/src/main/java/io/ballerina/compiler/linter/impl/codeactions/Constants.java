/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.linter.impl.codeactions;

/**
 * Constants used in Code Actions.
 *
 * @since 2.0.0
 */
public interface Constants {

    // Names
    String CA_CHECK_USAGE = "check-usage-code-action";
    String CA_FLOATING_POINT = "floating-point-code-action";
    String CA_QUALIFIED_IDENTIFIER = "qualified-identifier-code-action";

    // Diagnostics code
    String BCE_MISSING_HEX_DIGIT_AFTER_DOT = "BCE0410";
    String BCE_MISSING_DIGIT_AFTER_DOT = "BCE0416";
    String BCE_INTERVENING_WS = "BCE0676";
    String BCE_INVALID_WS = "BCE0651";
    String BCE_INVALID_CHECK = "BCE20404";

    // Code Actions
    String REMOVE_S_KEYWORD = "Remove '%s' keyword";
    String REPLACE_WITH = "Replace with '%s'";

    // Other Constants
    String DOT = ".";
    String DOT_AND_ZERO = ".0";
    String VALUE = "value";
    String LINE_RANGE = "lineRange";
}
