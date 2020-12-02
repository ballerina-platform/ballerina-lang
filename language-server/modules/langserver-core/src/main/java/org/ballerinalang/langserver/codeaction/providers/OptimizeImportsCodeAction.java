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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Code Action for optimizing all imports.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class OptimizeImportsCodeAction extends AbstractCodeActionProvider {
    private static final String UNUSED_IMPORT_MODULE = "unused import module";
    private static final String ALIAS_SEPARATOR = "as";


    public OptimizeImportsCodeAction() {
        super(Collections.singletonList(CodeActionNodeType.IMPORTS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.fileUri();
        SyntaxTree syntaxTree = context.workspace().syntaxTree(context.filePath()).orElseThrow();
        NodeList<ImportDeclarationNode> fileImports = ((ModulePartNode) syntaxTree.rootNode()).imports();

        if (fileImports == null || fileImports.isEmpty()) {
            return actions;
        }

        List<String[]> toBeRemovedImports = extractImportsToBeRemoved(context.allDiagnostics());

        // Skip, when nothing to remove and only single import pending
        if (fileImports.size() <= 1 && toBeRemovedImports.size() == 0) {
            return actions;
        }

        // Find the imports range
        int importSLine = fileImports.get(0).lineRange().startLine().line();
        List<Range> importLines = new ArrayList<>();
        for (int i = 0; i < fileImports.size(); i++) {
            ImportDeclarationNode importPkg = fileImports.get(i);
            LineRange pos = importPkg.lineRange();
            ImportModel importModel = ImportModel.from(importPkg);

            // Get imports starting line
            if (importSLine > pos.startLine().line()) {
                importSLine = pos.startLine().line();
            }

            // Mark locations of the imports
            importLines.add(CommonUtil.toRange(pos));

            // Remove any matching imports on-the-go
            boolean rmMatched = toBeRemovedImports.stream()
                    .anyMatch(rmImport -> rmImport[0].equals(importModel.orgName + importModel.moduleName) &&
                            rmImport[1].equals(importModel.version) && rmImport[2].equals(importModel.alias)
                    );
            if (rmMatched) {
                fileImports = fileImports.remove(i);
                i--;
            }
        }

        // Re-create imports list text
        StringJoiner editText = new StringJoiner(System.lineSeparator());
        sortImports(fileImports).forEach(importNode -> editText.add(importNode.toSourceCode()));

        Position position = new Position(importSLine, 0);
        List<TextEdit> edits = getImportsRemovalTextEdits(importLines);
        edits.add(new TextEdit(new Range(position, position), editText.toString()));
        actions.add(createQuickFixCodeAction(CommandConstants.OPTIMIZE_IMPORTS_TITLE, edits, uri));
        return actions;
    }

    private List<TextEdit> getImportsRemovalTextEdits(List<Range> importLines) {
        List<TextEdit> edits = new ArrayList<>();
        Range txtRange = null;
        for (Range importRange : importLines) {
            if (txtRange != null && importRange.getStart().getLine() != txtRange.getEnd().getLine() + 1) {
                edits.add(new TextEdit(new Range(txtRange.getStart(), txtRange.getEnd()), ""));
                txtRange = importRange;
            } else {
                if (txtRange == null) {
                    txtRange = importRange;
                } else {
                    txtRange.setEnd(importRange.getEnd());
                }
            }
        }
        if (txtRange != null) {
            edits.add(new TextEdit(new Range(txtRange.getStart(), txtRange.getEnd()), ""));
        }
        return edits;
    }

    private List<ImportDeclarationNode> sortImports(NodeList<ImportDeclarationNode> fileImports) {
        List<ImportDeclarationNode> allImports = new ArrayList<>();
        fileImports.iterator().forEachRemaining(allImports::add);
        return allImports.stream()
                .sorted(Comparator.comparing((Function<ImportDeclarationNode, String>) o -> o.orgName().isPresent() ?
                        o.orgName().get().orgName().text() : "")
                                .thenComparing(o -> o.prefix().isPresent() ? o.prefix().get().prefix().text() : ""))
                .collect(Collectors.toList());
    }

    private List<String[]> extractImportsToBeRemoved(List<Diagnostic> allDiagnotics) {
        List<String[]> importsToBeRemoved = new ArrayList<>();

        // Filter unused imports
        for (Diagnostic diag : allDiagnotics) {
            if (diag.getMessage().startsWith(UNUSED_IMPORT_MODULE)) {
                Matcher matcher = CommandConstants.UNUSED_IMPORT_MODULE_PATTERN.matcher(diag.getMessage());
                if (matcher.find()) {
                    String pkgName = matcher.group(1).trim();
                    String version = matcher.groupCount() > 1 && matcher.group(2) != null ? matcher.group(2) : "";
                    String alias = matcher.groupCount() > 2 && matcher.group(3) != null
                            ? matcher.group(3).replace(ALIAS_SEPARATOR + " ", "")
                            : "";
                    int aliasIndex = version.indexOf(" " + ALIAS_SEPARATOR + " ");
                    if (aliasIndex > 0) {
                        alias = version.substring(aliasIndex + 1).replace(ALIAS_SEPARATOR + " ", "");
                        version = version.substring(0, aliasIndex);
                    }
                    importsToBeRemoved.add(new String[]{pkgName, version, alias});
                }
            }
        }

        return importsToBeRemoved;
    }
}
