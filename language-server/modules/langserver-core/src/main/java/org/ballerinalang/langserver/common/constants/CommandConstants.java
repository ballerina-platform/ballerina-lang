/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.constants;

import java.util.regex.Pattern;

/**
 * Constants related to {@link org.eclipse.lsp4j.Command}.
 * @since v0.964.0
 */
public class CommandConstants {
    public static final String UNDEFINED_MODULE = "undefined module";
    public static final String UNDEFINED_FUNCTION = "undefined function";
    public static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";
    public static final String UNRESOLVED_MODULE = "cannot resolve module";
    public static final Pattern UNRESOLVED_MODULE_PATTERN = Pattern.compile("cannot resolve module '(.*)'");

    // Command Arguments
    public static final String ARG_KEY_DOC_URI = "doc.uri";

    public static final String ARG_KEY_MODULE_NAME = "module";

    public static final String ARG_KEY_FUNC_NAME = "function.name";

    public static final String ARG_KEY_FUNC_LOCATION = "function.location";

    public static final String ARG_KEY_RETURN_TYPE = "function.returns";

    public static final String ARG_KEY_VAR_NAME = "var.name";

    public static final String ARG_KEY_RETURN_DEFAULT_VAL = "function.returns.default";

    public static final String ARG_KEY_FUNC_ARGS = "function.arguments";

    public static final String ARG_KEY_NODE_TYPE = "node.type";

    public static final String ARG_KEY_NODE_LINE = "node.line";


    // Command Titles
    public static final String IMPORT_MODULE_TITLE = "Import Module ";

    public static final String CREATE_VARIABLE_TITLE = "Create Variable";

    public static final String CREATE_FUNCTION_TITLE = "Create Function ";

    public static final String ADD_DOCUMENTATION_TITLE = "Document This";

    public static final String ADD_ALL_DOC_TITLE = "Document All";

    public static final String CREATE_CONSTRUCTOR_TITLE = "Create Constructor";

    public static final String PULL_MOD_TITLE = "Pull from Ballerina Central";

    // Commands List
    public static final String CMD_IMPORT_MODULE = "IMPORT_MODULE";

    public static final String CMD_CREATE_FUNCTION = "CREATE_FUNC";

    public static final String CMD_CREATE_VARIABLE = "CREATE_VAR";

    public static final String CMD_ADD_DOCUMENTATION = "ADD_DOC";

    public static final String CMD_ADD_ALL_DOC = "ADD_ALL_DOC";

    public static final String CMD_CREATE_CONSTRUCTOR = "CREATE_CONSTRUCTOR";

    public static final String CMD_PULL_MODULE = "PULL_MODULE";
}
