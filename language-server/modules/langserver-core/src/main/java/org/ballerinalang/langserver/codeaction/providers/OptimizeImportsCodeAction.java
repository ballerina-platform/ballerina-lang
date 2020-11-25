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
    private static final String IMPORT_KW = "import";
    private static final String VERSION_KW = "version";
    private static final String ORG_SEPARATOR = "/";
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

        List<String[]> toBeRemovedImports = new ArrayList<>();

        // Filter unused imports
        for (Diagnostic diag : context.allDiagnostics()) {
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
                    toBeRemovedImports.add(new String[]{pkgName, version, alias});
                }
            }
        }

        // Skip, when nothing to remove and only single import pending
        if (fileImports.size() <= 1 && toBeRemovedImports.size() == 0) {
            return actions;
        }

        List<TextEdit> edits = new ArrayList<>();

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
            Range range = new Range(new Position(pos.startLine().line(), pos.startLine().offset()),
                                    new Position(pos.endLine().line(), pos.endLine().offset()));
            importLines.add(range);

            // Remove any matching imports on-the-go
            boolean rmMatched = toBeRemovedImports.stream()
                    .anyMatch(rmImport -> rmImport[0].equals(importModel.orgName + importModel.moduleName) &&
                            rmImport[1].equals(importModel.version) && rmImport[2].equals(importModel.alias)
                    );
            if (rmMatched) {
                fileImports.remove(i);
                i--;
            }
        }

        // Re-order imports
        List<ImportDeclarationNode> allImports = new ArrayList<>();
        fileImports.iterator().forEachRemaining(allImports::add);
        final List<ImportDeclarationNode> orderedImports = allImports.stream()
                .sorted(Comparator.comparing((Function<ImportDeclarationNode, String>) o -> o.orgName().isPresent() ?
                        o.orgName().get().orgName().text() : "")
                        .thenComparing(o -> o.prefix().isPresent() ? o.prefix().get().prefix().text() : ""))
                .collect(Collectors.toList());

        // Mark import removal ranges
        Range txtTange = null;
        for (Range importRange : importLines) {
            if (txtTange != null && importRange.getStart().getLine() != txtTange.getEnd().getLine() + 1) {
                edits.add(new TextEdit(new Range(txtTange.getStart(), txtTange.getEnd()), ""));
                txtTange = importRange;
            } else {
                if (txtTange == null) {
                    txtTange = importRange;
                } else {
                    txtTange.setEnd(importRange.getEnd());
                }
            }
        }
        if (txtTange != null) {
            edits.add(new TextEdit(new Range(txtTange.getStart(), txtTange.getEnd()), ""));
        }

        // Re-create imports list text
        StringJoiner editText = new StringJoiner(System.lineSeparator());
        for (ImportDeclarationNode importPkg : orderedImports) {
            ImportModel importModel = ImportModel.from(importPkg);
            String importText = IMPORT_KW + " " + importModel.orgName + importModel.moduleName;
            if (!importModel.version.isEmpty()) {
                importText += " " + VERSION_KW + " " + importModel.version;
            }
            if (!importModel.alias.isEmpty()) {
                importText += " " + ALIAS_SEPARATOR + " " + importModel.alias;
            }
            importText += ";";
            editText.add(importText);
        }
        Position position = new Position(importSLine, 0);
        edits.add(new TextEdit(new Range(position, position), editText.toString()));
        actions.add(createQuickFixCodeAction(CommandConstants.OPTIMIZE_IMPORTS_TITLE, edits, uri));
        return actions;
    }
}
