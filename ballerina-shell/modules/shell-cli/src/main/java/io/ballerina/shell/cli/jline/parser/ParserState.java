/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.jline.parser;

/**
 * State of the parser.
 *
 * @since 2.0.0
 */
public enum ParserState {
    NORMAL,
    AFTER_BACKWARD_SLASH, // following \
    IN_DOUBLE_QUOTES, // in double quotes
    IN_DOUBLE_QUOTES_AFTER_BACKWARD_SLASH, // in double quotes, following \
    IN_TEMPLATE, // in template (i.e. following `)
    IN_TEMPLATE_AFTER_DOLLAR, // in template, following $
    AFTER_FORWARD_SLASH, // following /
    IN_COMMENT, // in comment (either # or // style),
    AFTER_OPERATOR, // normal state but following operators
    ERROR
}
