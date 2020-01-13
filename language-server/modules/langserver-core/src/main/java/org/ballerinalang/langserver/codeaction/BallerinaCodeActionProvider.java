/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the SPI interface for the Ballerina Code Action Provider.
 *
 * @since 1.1.0
 */
public abstract class BallerinaCodeActionProvider {
    List<CodeActionNodeType> codeActionNodeTypes;
    private boolean isNodeBased = true;

    public BallerinaCodeActionProvider() {
        this(new ArrayList<>());
        this.isNodeBased = false;
    }

    /**
     * @param nodeTypes code action node types list
     */
    public BallerinaCodeActionProvider(List<CodeActionNodeType> nodeTypes) {
        this.codeActionNodeTypes = nodeTypes;
    }

    /**
     * returns the list of code actions based on node type or diagnostics.
     *
     * @param nodeType    code action node type
     * @param lsContext   language server context
     * @param diagnostics diagnostics list
     * @return list of Code Actions
     */
    protected abstract List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                       List<Diagnostic> diagnostics);

    protected final boolean isNodeBased() {
        return this.isNodeBased;
    }

    /**
     * returns the list of node types that the code action belongs to.
     *
     * @return list of code action node type
     */
    protected final List<CodeActionNodeType> getCodeActionNodeTypes() {
        return this.codeActionNodeTypes;
    }
}
