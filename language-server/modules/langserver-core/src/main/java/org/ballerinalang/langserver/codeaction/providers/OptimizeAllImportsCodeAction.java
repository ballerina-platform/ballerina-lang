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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
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
    private static final String IMPORT_KW = "import";
    private static final String VERSION_KW = "version";
    private static final String ORG_SEPARATOR = "/";
    private static final String ALIAS_SEPARATOR = "as";

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

        List<String[]> toBeRemovedImports = new ArrayList<>();

        // Filter unused imports
        for (Diagnostic diagnostic : allDiagnostics) {
            if (diagnostic.getMessage().startsWith(UNUSED_IMPORT_MODULE)) {
                Matcher matcher = CommandConstants.UNUSED_IMPORT_MODULE_PATTERN.matcher(diagnostic.getMessage());
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
        int importSLine = fileImports.get(0).pos.sLine - 1;
        List<Range> importLines = new ArrayList<>();
        for (int i = 0; i < fileImports.size(); i++) {
            BLangImportPackage importPkg = fileImports.get(i);
            DiagnosticPos pos = importPkg.getPosition();
            String orgName = importPkg.orgName.value;
            String pkgName = importPkg.pkgNameComps.stream().map(s -> s.value).collect(Collectors.joining("."));
            String alias = (pkgName.equals(importPkg.alias.value)) ? "" : importPkg.alias.value;
            String version = importPkg.version.value;

            // Get imports starting line
            if (importSLine > pos.sLine) {
                importSLine = pos.sLine - 1;
            }

            // Mark locations of the imports
            Range range = new Range(new Position(pos.sLine - 1, pos.sCol - 1),
                                    new Position(pos.eLine - 1, pos.eCol - 1));
            importLines.add(range);

            // Remove any matching imports on-the-go
            boolean rmMatched = toBeRemovedImports.stream()
                    .anyMatch(rmImport -> rmImport[0].equals(orgName + ORG_SEPARATOR + pkgName) &&
                            rmImport[1].equals(version) && rmImport[2].equals(alias)
                    );
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
        for (BLangImportPackage importPkg : orderedImports) {
            String pkgName = importPkg.pkgNameComps.stream().map(s -> s.value).collect(Collectors.joining("."));
            String alias = (pkgName.equals(importPkg.alias.value)) ? "" : importPkg.alias.value;
            String version = importPkg.version.value;
            String importText = IMPORT_KW + " " + importPkg.orgName.value + ORG_SEPARATOR + pkgName;
            if (!version.isEmpty()) {
                importText += " " + VERSION_KW + " " + version;
            }
            if (!alias.isEmpty()) {
                importText += " " + ALIAS_SEPARATOR + " " + alias;
            }
            importText += ";";
            editText.add(importText);
        }
        Position position = new Position(importSLine, 0);
        edits.add(new TextEdit(new Range(position, position), editText.toString()));
        actions.add(createQuickFixCodeAction(CommandConstants.OPTIMIZE_IMPORTS_TITLE, edits, uri));
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
