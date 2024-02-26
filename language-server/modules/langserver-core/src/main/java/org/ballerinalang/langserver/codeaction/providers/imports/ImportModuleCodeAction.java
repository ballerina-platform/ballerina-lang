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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.Name;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code Action for importing a module.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImportModuleCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Import Module";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DiagnosticErrorCode.UNDEFINED_MODULE.diagnosticId().equals(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        String uri = context.fileUri();
        if (context.currentSyntaxTree().isEmpty()) {
            return Collections.emptyList();
        }

        // Find the qualified name reference node within the diagnostic location
        Range diagRange = PositionUtil.toRange(diagnostic.location().lineRange());
        NonTerminalNode node = CommonUtil.findNode(diagRange, context.currentSyntaxTree().get());
        QNameRefFinder finder = new QNameRefFinder(diagnostic.properties().get(0).value());
        node.accept(finder);
        Optional<QualifiedNameReferenceNode> qNameReferenceNode = finder.getQNameReferenceNode();
        if (qNameReferenceNode.isEmpty()) {
            return Collections.emptyList();
        }

        String modulePrefix = qNameReferenceNode.get().modulePrefix().text();

        List<LSPackageLoader.ModuleInfo> moduleList = LSPackageLoader
                .getInstance(context.languageServercontext()).getAllVisiblePackages(context);

        // Check if we already have packages imported with the given module prefix but with different aliases
        Map<ImportDeclarationNode, ModuleSymbol> symbolMap = context.currentDocImportsMap().entrySet().stream()
                .filter(entry -> modulePrefix
                        .equals(entry.getKey().moduleName().get(entry.getKey().moduleName().size() - 1).text()))
                .filter(entry -> entry.getKey().prefix().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<ModuleSymbol> existingModules = symbolMap.values().stream()
                .filter(moduleSymbol -> moduleSymbol.getModule().isPresent())
                .map(moduleSymbol -> moduleSymbol.getModule().get()).collect(Collectors.toList());

        List<CodeAction> actions = new ArrayList<>();

        symbolMap.forEach((importNode, moduleSymbol) -> {
            if (importNode.prefix().isEmpty()) {
                return;
            }
            ImportPrefixNode prefixNode = importNode.prefix().get();
            Token prefix = prefixNode.prefix();
            if (prefix.kind() == SyntaxKind.UNDERSCORE_KEYWORD) {
                int startOffset = importNode.moduleName().get(importNode.moduleName().size() - 1)
                        .textRange().endOffset();;
                Range insertRange = PositionUtil.toRange(startOffset,
                        prefixNode.textRange().endOffset(), context.currentSyntaxTree().get().textDocument());
                List<TextEdit> edits = Collections.singletonList(new TextEdit(insertRange, ""));
                CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.REMOVE_MODULE_ALIAS,
                        edits, uri, CodeActionKind.QuickFix);
                actions.add(codeAction);
            } else {
                String modPrefix = moduleSymbol.id().modulePrefix();
                Range insertRange = PositionUtil.toRange(qNameReferenceNode.get().modulePrefix().lineRange());
                List<TextEdit> edits = Collections.singletonList(new TextEdit(insertRange, modPrefix));
                CodeAction codeAction = CodeActionUtil.createCodeAction(
                        String.format(CommandConstants.CHANGE_MODULE_PREFIX_TITLE, modPrefix),
                        edits, uri, CodeActionKind.QuickFix);
                actions.add(codeAction);
            }
        });

        // Here we filter out the already imported packages
        moduleList.stream()
                .filter(pkgEntry -> existingModules.stream()
                        .noneMatch(moduleSymbol -> moduleSymbol.id().orgName().equals(pkgEntry.packageOrg().value()) &&
                                moduleSymbol.id().moduleName().equals(pkgEntry.packageName().value()))
                )
                .filter(pkgEntry -> {
                    String pkgName = pkgEntry.packageName().value();
                    return pkgName.endsWith("." + modulePrefix) || pkgName.equals(modulePrefix);
                })
                .forEach(pkgEntry -> {
                    String orgName = pkgEntry.packageOrg().value();
                    String pkgName = pkgEntry.packageName().value();
                    String moduleName = ModuleUtil.escapeModuleName(pkgName);
                    Position insertPos = CommonUtil.getImportPosition(context);
                    String importText = orgName.isEmpty() ?
                            String.format("%s %s;%n", ItemResolverConstants.IMPORT, moduleName)
                            : String.format("%s %s/%s;%n", ItemResolverConstants.IMPORT, orgName, moduleName);
                    String commandTitle = orgName.isEmpty() ? String.format(CommandConstants.IMPORT_MODULE_TITLE,
                            moduleName) : String.format(CommandConstants.IMPORT_MODULE_TITLE,
                            orgName + "/" + moduleName);
                    List<TextEdit> edits = Collections.singletonList(
                            new TextEdit(new Range(insertPos, insertPos), importText));
                    CodeAction action = CodeActionUtil
                            .createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix);
                    actions.add(action);
                });
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * A visitor to find the qualified name reference node within an expression.
     */
    static class QNameRefFinder extends NodeVisitor {
        private final String moduleName;

        public QNameRefFinder(Object nameObj) {
            if (nameObj instanceof Name name) {
                this.moduleName = name.getValue();
            } else if (nameObj instanceof BLangIdentifier identifier) {
                this.moduleName = identifier.getValue();
            } else {
                this.moduleName = nameObj.toString();
            }
        }

        private QualifiedNameReferenceNode qualifiedNameReferenceNode;

        @Override
        public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            if (qualifiedNameReferenceNode.modulePrefix().text().equals(moduleName)) {
                this.qualifiedNameReferenceNode = qualifiedNameReferenceNode;
            }
        }

        Optional<QualifiedNameReferenceNode> getQNameReferenceNode() {
            return Optional.ofNullable(this.qualifiedNameReferenceNode);
        }
    }
}
