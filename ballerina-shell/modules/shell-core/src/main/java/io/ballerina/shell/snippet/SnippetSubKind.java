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

package io.ballerina.shell.snippet;

/**
 * Snippet sub type to further categorize snippets.
 *
 * @since 2.0.0
 */
public enum SnippetSubKind {
    IMPORT_DECLARATION(SnippetKind.IMPORT_DECLARATION),

    VARIABLE_DECLARATION(SnippetKind.VARIABLE_DECLARATION),

    // Module member declarations - none is executable
    // TODO: Verify that listeners are not executable
    // TODO: Support service declarations?
    FUNCTION_DEFINITION(SnippetKind.MODULE_MEMBER_DECLARATION),
    LISTENER_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION),
    TYPE_DEFINITION(SnippetKind.MODULE_MEMBER_DECLARATION),
    SERVICE_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION, "Services are not allowed within REPL."), // Error
    CONSTANT_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION),
    MODULE_VARIABLE_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION, true), // Ignore
    ANNOTATION_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION),
    MODULE_XML_NAMESPACE_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION),
    ENUM_DECLARATION(SnippetKind.MODULE_MEMBER_DECLARATION),
    CLASS_DEFINITION(SnippetKind.MODULE_MEMBER_DECLARATION),

    // Statements - everything is executable
    ASSIGNMENT_STATEMENT(SnippetKind.STATEMENT),
    COMPOUND_ASSIGNMENT_STATEMENT(SnippetKind.STATEMENT),
    VARIABLE_DECLARATION_STATEMENT(SnippetKind.STATEMENT, true), // Ignore
    BLOCK_STATEMENT(SnippetKind.STATEMENT),
    BREAK_STATEMENT(SnippetKind.STATEMENT),
    FAIL_STATEMENT(SnippetKind.STATEMENT),
    EXPRESSION_STATEMENT(SnippetKind.STATEMENT, true), // Ignore
    CONTINUE_STATEMENT(SnippetKind.STATEMENT),
    IF_ELSE_STATEMENT(SnippetKind.STATEMENT),
    WHILE_STATEMENT(SnippetKind.STATEMENT),
    PANIC_STATEMENT(SnippetKind.STATEMENT),
    RETURN_STATEMENT(SnippetKind.STATEMENT),
    LOCAL_TYPE_DEFINITION_STATEMENT(SnippetKind.STATEMENT, true), // Ignore
    LOCK_STATEMENT(SnippetKind.STATEMENT),
    FORK_STATEMENT(SnippetKind.STATEMENT),
    FOR_EACH_STATEMENT(SnippetKind.STATEMENT),
    XML_NAMESPACE_DECLARATION_STATEMENT(SnippetKind.STATEMENT, true), // Ignore
    TRANSACTION_STATEMENT(SnippetKind.STATEMENT),
    ROLLBACK_STATEMENT(SnippetKind.STATEMENT),
    RETRY_STATEMENT(SnippetKind.STATEMENT),
    MATCH_STATEMENT(SnippetKind.STATEMENT),
    DO_STATEMENT(SnippetKind.STATEMENT),

    EXPRESSION(SnippetKind.EXPRESSION);

    private final SnippetKind kind;
    private final boolean isIgnored;
    private final String error;

    SnippetSubKind(SnippetKind kind) {
        this.kind = kind;
        this.isIgnored = false;
        this.error = null;
    }

    SnippetSubKind(SnippetKind kind, boolean isIgnored) {
        this.kind = kind;
        this.isIgnored = isIgnored;
        this.error = null;
    }

    SnippetSubKind(SnippetKind kind, String error) {
        this.kind = kind;
        this.isIgnored = false;
        this.error = error;
    }

    public SnippetKind getKind() {
        return kind;
    }

    public boolean isValid() {
        return !isIgnored;
    }

    public boolean hasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }
}
