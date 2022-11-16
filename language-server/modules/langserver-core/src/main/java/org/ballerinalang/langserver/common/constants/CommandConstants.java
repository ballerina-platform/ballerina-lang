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
 *
 * @since v0.964.0
 */
public class CommandConstants {

    public static final String UNDEFINED_MODULE = "undefined module";
    public static final String UNDEFINED_FUNCTION = "undefined function";
    public static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";
    public static final String UNRESOLVED_MODULE = "cannot resolve module";
    public static final String TAINTED_PARAM_PASSED = "tainted value passed to untainted parameter";
    public static final String NO_IMPL_FOUND_FOR_METHOD = "no implementation found for the method";
    public static final String FUNC_IMPL_FOUND_IN_ABSTRACT_OBJ = "cannot have a body";
    public static final Pattern UNUSED_IMPORT_MODULE_PATTERN = Pattern.compile(
            "unused import module '(\\S*)\\s*(?:version\\s(.*))?(.*)'");
    public static final Pattern UNRESOLVED_MODULE_PATTERN = Pattern.compile(
            "cannot resolve module '(\\S*)\\s*(?:version\\s(.*))?(.*)'");
    public static final Pattern TAINTED_PARAM_PATTERN = Pattern.compile(
            "tainted value passed to untainted parameter '(.*)'");
    public static final Pattern UNDEFINED_FUNCTION_PATTERN = Pattern.compile("undefined function '(.*)'");
    public static final String INCOMPATIBLE_TYPES = "incompatible types";
    public static final Pattern INCOMPATIBLE_TYPE_PATTERN = Pattern.compile(
            "incompatible types: expected '(.*)', found '(.*)'");
    public static final Pattern NO_IMPL_FOUND_FOR_FUNCTION_PATTERN = Pattern.compile(
            "no implementation found for the method '(.*)' of class '(.*)'");
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

    public static final String ARG_KEY_NODE_POS = "node.position";

    public static final String ARG_KEY_NODE_RANGE = "node.range";

    public static final String ARG_KEY_MESSAGE_TYPE = "message.type";

    public static final String ARG_KEY_MESSAGE = "message";

    public static final String ARG_KEY_PATH = "path";

    public static final String ARG_KEY_METHOD = "method";

    public static final String ARG_KEY_PARAMETER = "parameter";

    // Command Titles
    public static final String IMPORT_MODULE_TITLE = "Import module '%s'";

    public static final String CHANGE_MODULE_PREFIX_TITLE = "Change module prefix to '%s'";

    public static final String CREATE_VARIABLE_TITLE = "Create variable";

    public static final String IGNORE_RETURN_TITLE = "Ignore return value";

    public static final String CREATE_FUNCTION_TITLE = "Create function '%s'";

    public static final String MARK_UNTAINTED_TITLE = "Mark '%s' as untainted";

    public static final String CREATE_TEST_FUNC_TITLE = "Create test for function";

    public static final String CREATE_TEST_SERVICE_TITLE = "Create test for service";

    public static final String ADD_DOCUMENTATION_TITLE = "Document this";

    public static final String UPDATE_DOCUMENTATION_TITLE = "Update documentation";

    public static final String ADD_ALL_DOC_TITLE = "Document all";

    public static final String CREATE_INITIALIZER_TITLE = "Create initializer";

    public static final String PULL_MOD_TITLE = "Pull unresolved modules";

    public static final String CHANGE_RETURN_TYPE_TITLE = "Change return type to '%s'";

    public static final String ADD_TYPE_CAST_TITLE = "Add type cast";

    public static final String ADD_TYPE_CAST_TO_NUMERIC_OPERAND_TITLE = "Add type cast to '%s'";

    public static final String CHANGE_VAR_TYPE_TITLE = "Change variable '%s' type to '%s'";

    public static final String CHANGE_CONST_TYPE_TITLE = "Change constant '%s' type to '%s'";

    public static final String CHANGE_PARAM_TYPE_TITLE = "Change parameter '%s' type to '%s'";

    public static final String CREATE_VAR_TYPE_GUARD_TITLE = "Create variable and type guard";

    public static final String TYPE_GUARD_TITLE = "Type guard variable '%s'";

    public static final String CREATE_VAR_ADD_CHECK_TITLE = "Create variable and check error";

    public static final String ADD_CHECK_TITLE = "Add 'check' error";

    public static final String CREATE_SERVICE_RESOURCE = "Create service resource for the path '%s'";

    public static final String CREATE_SERVICE_RESOURCE_METHOD =
            "Create service resource for http method '%s' for the path '%s'";

    public static final String ADD_MISSING_PARAMETER_IN_BALLERINA =
            "Add missing parameter '%s' for the method '%s' for the path '%s'";

    public static final String IMPLEMENT_FUNCS_TITLE = "Implement method '%s'";

    public static final String OPTIMIZE_IMPORTS_TITLE = "Optimize all imports";

    public static final String REMOVE_ALL_UNUSED_IMPORTS = "Remove all unused imports";

    public static final String REMOVE_UNUSED_IMPORT = "Remove unused import '%s'";

    public static final String REMOVE_REDECLARED_IMPORT = "Remove re-declared import '%s'";

    public static final String REPORT_USAGE_STATISTICS_COMMAND_TITLE = "Report usage statistics";

    public static final String CONVERT_FUNCTION_TO_PUBLIC = "Convert to public function";

    public static final String CREATE_ON_FAIL_CLAUSE = "Create on fail clause";

    public static final String SURROUND_WITH_DO_ON_FAIL = "Surround with do/on-fail";

    public static final String CONVERT_MODULE_VAR_TO_LISTENER_DECLARATION =
            "Convert module variable '%s' to listener declaration";

    public static final String CONVERT_TO_READONLY_CLONE = "Convert to Readonly Clone";

    public static final String ADD_EXPLICIT_RETURN_STATEMENT = "Add Explicit Return Statement";

    public static final String REMOVE_UNREACHABLE_CODE_TITLE = "Remove unreachable code";

    public static final String IGNORE_UNUSED_VAR_TITLE = "Ignore unused variable";

    public static final String IMPLEMENT_ALL = "Implement all";

    public static final String MAKE_CONSTRUCT_PUBLIC = "Convert '%s' to public";

    public static final String MAKE_FUNCTION_ISOLATE = "Add isolated qualifier to '%s'";

    public static final String EXTRACT_TYPE = "Extract type";

    public static final String EXTRACT_TO_FUNCTION = "Extract to function";

    public static final String MAKE_ANNOT_DECL_CONST = "Convert '%s' to constant";

    public static final String ADD_CONDITIONAL_DEFAULT = "Add conditional default value";

    public static final String EXTRACT_TO_CONSTANT = "Extract to constant";

    public static final String EXTRACT_TO_VARIABLE = "Extract to local variable";

    public static final String GENERATE_MODULE_FOR_CLIENT_DECLARATION_TITLE = "Generate module for client declaration";

    public static final String FILL_REQUIRED_FIELDS = "Fill '%s' required fields";

    public static final String CHANGE_TO_SUBTYPE_OF_RAW_TEMPLATE_TITLE = "Convert to '%s' template";

    public static final String RENAME_COMMAND = "ballerina.action.rename";

    public static final String POSITIONAL_RENAME_COMMAND = "ballerina.action.positional.rename";

    public static final String RENAME_COMMAND_TITLE_FOR_VARIABLE = "Rename variable";

    public static final String RENAME_COMMAND_TITLE_FOR_CONSTANT = "Rename constant";

    public static final String RENAME_COMMAND_TITLE_FOR_FUNCTION = "Rename function";
}
