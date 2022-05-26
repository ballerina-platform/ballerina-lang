/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Code Action to remove all unused imports, except when there is a re-declared import statement.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class RemoveAllUnusedImportsCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Optimize Imports";
    public static final String DIAGNOSTIC_CODE = "BCE2002";

    public RemoveAllUnusedImportsCodeAction() {
        super(Collections.singletonList(CodeActionNodeType.IMPORTS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.fileUri();
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        // Copying to a separate list since there's side effects when modifying same node-list
        List<ImportDeclarationNode> fileImports = new ArrayList<>();
        ((ModulePartNode) syntaxTree.rootNode()).imports().stream().forEach(fileImports::add);

        List<LineRange> toBeRemovedImportsLocations = context.diagnostics(context.filePath()).stream()
                .filter(diag -> DIAGNOSTIC_CODE.equals(diag.diagnosticInfo().code()))
                .map(diag -> diag.location().lineRange())
                .collect(Collectors.toList());

        // Skip, when nothing to remove and only single import pending
        if (fileImports.isEmpty() || (fileImports.size() <= 1 && toBeRemovedImportsLocations.size() == 0)) {
            return Collections.emptyList();
        }

        // Find the imports range
        int importSLine = fileImports.get(0).lineRange().startLine().line();
        int importELine = fileImports.get(0).lineRange().endLine().line();
        for (int i = 0; i < fileImports.size(); i++) {
            ImportDeclarationNode importPkg = fileImports.get(i);
            LineRange pos = importPkg.lineRange();

            // Get imports starting line
            if (importSLine > pos.startLine().line()) {
                importSLine = pos.startLine().line();
            }

            // Get imports ending position
            if (importELine < pos.endLine().line()) {
                importELine = pos.endLine().line();
            }

            // Remove any matching imports on-the-go
            i = CodeActionUtil.removeImportAndUpdateIterator(fileImports, toBeRemovedImportsLocations, i, importPkg);
        }

        // Re-create imports list text
        StringBuilder editText = new StringBuilder();
        fileImports.forEach(importNode -> CodeActionUtil.buildEditText(editText, importNode));

        Position importStart = new Position(importSLine, 0);
        Position importEnd = new Position(importELine + 1, 0);
        TextEdit textEdit = new TextEdit(new Range(importStart, importEnd), editText.toString());
        List<TextEdit> edits = Collections.singletonList(textEdit);
        actions.add(createCodeAction(CommandConstants.REMOVE_ALL_UNUSED_IMPORTS, edits, uri, CodeActionKind.QuickFix));
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
