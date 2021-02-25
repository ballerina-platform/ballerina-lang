/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents the language server workspace service context.
 *
 * @since 2.0.0
 */
public interface WorkspaceServiceContext {

    /**
     * Get the symbols visible at a given cursor position of a given file.
     *
     * @param filePath  Path of the file in which the visible symbols are required to be found
     * @param position cursor position
     * @return {@link List}
     */
    List<Symbol> visibleSymbols(Path filePath, Position position);
    
    /**
     * Get the workspace manager instance.
     *
     * @return {@link WorkspaceManager} instance for the language server
     */
    WorkspaceManager workspace();

    /**
     * Get the operation.
     *
     * @return {@link LSOperation}
     */
    LSOperation operation();

    /**
     * Get the language server context.
     *
     * @return {@link LanguageServerContext}
     */
    LanguageServerContext languageServercontext();
}
