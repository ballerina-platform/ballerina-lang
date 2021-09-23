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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Package;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for importing a module.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImportModuleCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Import Module";

    private static final String UNDEFINED_MODULE = "undefined module";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        if (!(diagnostic.message().startsWith(UNDEFINED_MODULE))) {
            return actions;
        }
        String uri = context.fileUri();
        List<Diagnostic> diagnostics = new ArrayList<>();
        String diagnosticMessage = diagnostic.message();
        String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                diagnosticMessage.lastIndexOf("'"));
        LanguageServerContext serverContext = context.languageServercontext();
        List<Package> packagesList =
                new ArrayList<>(LSPackageLoader.getInstance(serverContext).getDistributionRepoPackages());

        packagesList.stream()
                .filter(pkgEntry -> {
                    String pkgName = pkgEntry.packageName().value();
                    return pkgName.endsWith("." + packageAlias) || pkgName.equals(packageAlias);
                })
                .forEach(pkgEntry -> {
                    String pkgName = pkgEntry.packageName().value();
                    String moduleName = CommonUtil.escapeModuleName(pkgName);
                    CodeAction action = new CodeAction();
                    Position insertPos = getImportPosition(context);
                    String importText = ItemResolverConstants.IMPORT + " " + pkgEntry.packageOrg().value() + "/"
                            + moduleName + ";" + CommonUtil.LINE_SEPARATOR;
                    String commandTitle = String.format(CommandConstants.IMPORT_MODULE_TITLE,
                                                        pkgEntry.packageOrg().value() + "/" + moduleName);
                    action.setTitle(commandTitle);
                    List<TextEdit> edits = Collections.singletonList(
                            new TextEdit(new Range(insertPos, insertPos), importText));
                    action.setKind(CodeActionKind.QuickFix);
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
                    action.setDiagnostics(CodeActionUtil.toDiagnostics(diagnostics));
                    actions.add(action);
                });
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static Position getImportPosition(CodeActionContext context) {
        // Calculate initial import insertion line
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        ModulePartNode modulePartNode = syntaxTree.orElseThrow().rootNode();
        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();
        if (imports.isEmpty()) {
            return new Position(0, 0);
        }
        ImportDeclarationNode lastImport = imports.get(imports.size() - 1);
        return new Position(lastImport.lineRange().endLine().line() + 1, 0);
    }
}
