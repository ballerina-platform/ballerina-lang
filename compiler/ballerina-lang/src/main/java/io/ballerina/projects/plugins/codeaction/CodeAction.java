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
package io.ballerina.projects.plugins.codeaction;

import java.util.List;
import java.util.Optional;

/**
 * Represents an action (named <strong>code action</strong> that can be performed by the user when writing ballerina
 * code using an IDE/editor with language server support. Usually these are used to provide quick fixes for
 * diagnostics. This interface provides methods to get details of the code action for a provided diagnostic and to
 * apply/execute the code action when the user clicks it.
 *
 * @since 2.0.0
 */
public interface CodeAction {

    /**
     * Set of interested diagnostic codes for this code action. This code action will be invoked only for diagnostics
     * with matching diagnostic codes.
     *
     * @return List of interested diagnostic codes
     */
    List<String> supportedDiagnosticCodes();

    /**
     * Returns the details of the code action depending on the current context (cursor position, surrounding syntax tree
     * nodes, etc) and diagnostic information.
     *
     * @param context Context representing the document, syntax tree, cursor position and etc
     * @return Optional code action details. Optional can be empty if the code action doesn't apply to the context
     */
    Optional<CodeActionInfo> codeActionInfo(CodeActionContext context);

    /**
     * Once the user accepts the quickfix (code action), this method is invoked to perform the required changes to the
     * document(s).
     *
     * @param context Code action context
     *                returning this code action
     * @return A list of document edits to be applied to the ballerina files in the project
     */
    List<DocumentEdit> execute(CodeActionExecutionContext context);

    /**
     * A unique name (within the compiler plugin) representing this code action. It will be used to uniquely identify
     * this code action during runtime.
     *
     * @return A unique (within the compiler plugin) name
     */
    String name();
}
