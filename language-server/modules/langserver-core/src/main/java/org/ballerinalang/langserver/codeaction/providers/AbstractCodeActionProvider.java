/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Represents the common class for the default Ballerina Code Action Providers.
 *
 * @since 1.1.1
 */
public abstract class AbstractCodeActionProvider implements LSCodeActionProvider {
    private List<CodeActionNodeType> codeActionNodeTypes;
    private final boolean isNodeTypeBased;

    /**
     * Create a diagnostic based code action provider.
     */
    public AbstractCodeActionProvider() {
        this.isNodeTypeBased = false;
    }

    /**
     * Create a node type based code action provider.
     *
     * @param nodeTypes code action node types list
     */
    public AbstractCodeActionProvider(List<CodeActionNodeType> nodeTypes) {
        this.codeActionNodeTypes = nodeTypes;
        this.isNodeTypeBased = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnostics);

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isNodeBasedSupported() {
        return this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDiagBasedSupported() {
        return !this.isNodeTypeBased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<CodeActionNodeType> getCodeActionNodeTypes() {
        return this.codeActionNodeTypes;
    }

    /**
     * Returns diagnostic message highlighted content.
     *
     * @param diagnostic {@link Diagnostic}
     * @param context    {@link LSContext}
     * @param document   {@link LSDocumentIdentifier}
     * @return diagnostic highlighted content
     */
    protected static String getDiagnosedContent(Diagnostic diagnostic, LSContext context,
                                                LSDocumentIdentifier document) {
        WorkspaceDocumentManager docManager = context.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        StringBuilder content = new StringBuilder();
        Position start = diagnostic.getRange().getStart();
        Position end = diagnostic.getRange().getEnd();
        try (BufferedReader reader = new BufferedReader(
                new StringReader(docManager.getFileContent(document.getPath())))) {
            String strLine;
            int count = 0;
            while ((strLine = reader.readLine()) != null) {
                if (count >= start.getLine() && count <= end.getLine()) {
                    if (count == start.getLine()) {
                        content.append(strLine.substring(start.getCharacter()));
                        if (start.getLine() != end.getLine()) {
                            content.append(System.lineSeparator());
                        }
                    } else if (count == end.getLine()) {
                        content.append(strLine.substring(0, end.getCharacter()));
                    } else {
                        content.append(strLine).append(System.lineSeparator());
                    }
                }
                if (count == end.getLine()) {
                    break;
                }
                count++;
            }
        } catch (WorkspaceDocumentException | IOException e) {
            // ignore error
        }
        return content.toString();
    }
}
