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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Code Action provider for optimizing all imports.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class OptimizeAllImportsCodeAction extends AbstractCodeActionProvider {
    private static final String UNUSED_IMPORT_MODULE = "unused import module";

    public OptimizeAllImportsCodeAction() {
        super(Collections.singletonList(CodeActionNodeType.IMPORTS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<BLangImportPackage> fileImports = CommonUtil.getCurrentFileImports(context);
        if (bLangPackage == null || fileImports == null || fileImports.isEmpty()) {
            return actions;
        }

        List<Pair<String, String>> toBeRemovedImports = new ArrayList<>();

        // Filter unused imports
        for (Diagnostic diagnostic : allDiagnostics) {
            if (diagnostic.getMessage().startsWith(UNUSED_IMPORT_MODULE)) {
                Matcher matcher = CommandConstants.UNUSED_IMPORT_MODULE_PATTERN.matcher(diagnostic.getMessage());
                if (matcher.find()) {
                    String pkgName = matcher.group(1).trim();
                    String version = matcher.groupCount() > 1 && matcher.group(2) != null ? ":" + matcher.group(2) :
                            "";
                    toBeRemovedImports.add(new ImmutablePair<>(pkgName, version));
                }
            }
        }

        // Skip, when nothing to remove and only single import pending
        if (fileImports.size() <= 1 && toBeRemovedImports.size() == 0) {
            return actions;
        }

        // Find the imports range
        int importSLine = 0;
        int importELine = 0;
        int importSCol = 0;
        int importECol = 0;
        for (int i = 0; i < fileImports.size(); i++) {
            BLangImportPackage importPkg = fileImports.get(i);
            DiagnosticPos pos = importPkg.getPosition();
            String orgName = importPkg.orgName.value;
            String alias = importPkg.alias.value;
            String version = importPkg.version.value;
            if (importSLine > pos.sLine) {
                importSLine = pos.sLine;
            }
            if (importELine < pos.eLine) {
                importELine = pos.eLine;
            }
            if (importECol < pos.eCol) {
                importECol = pos.eCol;
            }
            // Remove any matching imports on-the-go
            boolean rmMatched = toBeRemovedImports.stream()
                    .anyMatch(rmImport -> rmImport.getLeft().equals(orgName + "/" + alias) &&
                            rmImport.getRight().equals(version));
            if (rmMatched) {
                fileImports.remove(i);
                i--;
            }
        }

        // Re-order imports
        List<BLangImportPackage> orderedImports = fileImports.stream()
                .sorted(Comparator.comparing((Function<BLangImportPackage, String>) o -> o.orgName.value)
                                .thenComparing(o -> o.getAlias().value))
                .collect(Collectors.toList());

        // Create imports list text
        StringBuilder editText = new StringBuilder();
        for (BLangImportPackage importPkg : orderedImports) {
            editText.append("import ").append(importPkg.orgName.value).append("/").append(importPkg.alias.value);
            String version = importPkg.version.value;
            if (!version.isEmpty()) {
                editText.append(" version ").append(version);
            }
            editText.append(";").append(System.lineSeparator());
        }

        Range range = new Range(new Position(importSLine, importSCol), new Position(importELine, importECol));
        List<TextEdit> edits = Collections.singletonList(new TextEdit(range, editText.toString()));

        CodeAction action = new CodeAction(CommandConstants.OPTIMIZE_IMPORTS_TITLE);
        action.setKind(CodeActionKind.QuickFix);
        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
        action.setDiagnostics(null);
        actions.add(action);
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }
}
