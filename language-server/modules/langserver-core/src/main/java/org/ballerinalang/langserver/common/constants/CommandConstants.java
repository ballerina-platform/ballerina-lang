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
    public static final String TAINTED_PARAM_PASSED = "tainted value passed to untainted parameter";
    public static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";
    public static final String FUNC_IMPL_FOUND_IN_ABSTRACT_OBJ = "cannot have a body";
    public static final Pattern UNRESOLVED_MODULE_PATTERN = Pattern.compile(
            "cannot resolve module '([^(v)]*)(?:version (.*))?'");
    public static final Pattern TAINTED_PARAM_PATTERN = Pattern.compile(
            "tainted value passed to untainted parameter '(.*)'");
    public static final Pattern UNDEFINED_FUNCTION_PATTERN = Pattern.compile("undefined function '(.*)'");
    public static final String INCOMPATIBLE_TYPES = "incompatible types";
    public static final Pattern INCOMPATIBLE_TYPE_PATTERN = Pattern.compile(
            "incompatible types: expected '(.*)', found '(.*)'");
    public static final Pattern NO_IMPL_FOUND_FOR_FUNCTION_PATTERN = Pattern.compile(
            "no implementation found for the function '(.*)' of non-abstract object '(.*)'");
    public static final Pattern FUNC_IN_ABSTRACT_OBJ_PATTERN = Pattern.compile(
            "function '(.*)' in abstract object '(.*)' cannot have a body");
    public static final Pattern FQ_TYPE_PATTERN = Pattern.compile("(.*)/([^:]*):(?:.*:)?(.*)");
    public static final Pattern NO_CONCAT_PATTERN = Pattern.compile("^\\\"[^\\\"]*\\\"$|^[^\\\"\\+]*$");

    // Command Arguments
    public static final String ARG_KEY_DOC_URI = "doc.uri";

    public static final String ARG_KEY_MODULE_NAME = "module";

    public static final String ARG_KEY_SERVICE_NAME = "service.name";

    public static final String ARG_KEY_FUNCTION_NAME = "function.name";

    public static final String ARG_KEY_NODE_TYPE = "node.type";

    public static final String ARG_KEY_NODE_LINE = "node.line";

    public static final String ARG_KEY_NODE_COLUMN = "node.column";

    public static final String ARG_KEY_MESSAGE_TYPE = "message.type";

    public static final String ARG_KEY_MESSAGE = "message";

    // Command Titles
    public static final String IMPORT_MODULE_TITLE = "Import Module ";

    public static final String CREATE_VARIABLE_TITLE = "Create Local Variable";

    public static final String IGNORE_RETURN_TITLE = "Ignore Return Value";

    public static final String CREATE_FUNCTION_TITLE = "Create Function ";

    public static final String MARK_UNTAINTED_TITLE = "Mark '%s' as Untainted";

    public static final String CREATE_TEST_FUNC_TITLE = "Create Test For Function";

    public static final String CREATE_TEST_SERVICE_TITLE = "Create Test For Service";

    public static final String ADD_DOCUMENTATION_TITLE = "Document This";

    public static final String ADD_ALL_DOC_TITLE = "Document All";

    public static final String CREATE_INITIALIZER_TITLE = "Create Initializer";

    public static final String PULL_MOD_TITLE = "Pull from Ballerina Central";

    public static final String CHANGE_RETURN_TYPE_TITLE = "Change Return Type to '";

    public static final String MAKE_OBJ_ABSTRACT_TITLE = "Make '%s' an Abstract Object";

    public static final String MAKE_OBJ_NON_ABSTRACT_TITLE = "Make '%s' an Non-Abstract Object";

    public static final String TYPE_GUARD_TITLE = "Type Guard '%s'";
}
