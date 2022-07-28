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
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.lang3.tuple.Pair;
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
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

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
        QNameRefFinder finder = new QNameRefFinder();
        node.accept(finder);
        Optional<QualifiedNameReferenceNode> qNameReferenceNode = finder.getQNameReferenceNode();
        if (qNameReferenceNode.isEmpty()) {
            return Collections.emptyList();
        }

        String modulePrefix = qNameReferenceNode.get().modulePrefix().text();

        List<LSPackageLoader.PackageInfo> packagesList = LSPackageLoader
                .getInstance(context.languageServercontext()).getAllVisiblePackages(context);

        // Check if we already have packages imported with the given module prefix but with different aliases
        List<ModuleSymbol> existingModules = context.currentDocImportsMap().entrySet().stream()
                .filter(entry -> modulePrefix
                        .equals(entry.getKey().moduleName().get(entry.getKey().moduleName().size() - 1).text()))
                .filter(entry -> entry.getKey().prefix().isPresent())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // List of existing mod aliases with the given module prefix
        List<String> existingPrefixes = existingModules.stream()
                .map(mod -> mod.id().modulePrefix())
                .collect(Collectors.toList());

        List<CodeAction> actions = new ArrayList<>();

        // If there are already imports with the module prefix, but with aliases, we suggest to use that instead
        for (String existingPrefix : existingPrefixes) {
            Range insertRange = PositionUtil.toRange(qNameReferenceNode.get().modulePrefix().lineRange());
            List<TextEdit> edits = Collections.singletonList(new TextEdit(insertRange, existingPrefix));
            CodeAction codeAction = CodeActionUtil.createCodeAction(
                    String.format(CommandConstants.CHANGE_MODULE_PREFIX_TITLE, existingPrefix),
                    edits, uri, CodeActionKind.QuickFix);
            actions.add(codeAction);
        }

        // Here we filter out the already imported packages
        packagesList.stream()
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
                    Position insertPos = getImportPosition(context);
                    String importText = ItemResolverConstants.IMPORT + " " + orgName + "/"
                            + moduleName + ";" + CommonUtil.LINE_SEPARATOR;
                    String commandTitle = String.format(CommandConstants.IMPORT_MODULE_TITLE,
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

    /**
     * A visitor to find the qualified name reference node within an expression
     */
    static class QNameRefFinder extends NodeVisitor {

        private QualifiedNameReferenceNode qualifiedNameReferenceNode;

        @Override
        public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            this.qualifiedNameReferenceNode = qualifiedNameReferenceNode;
        }

        Optional<QualifiedNameReferenceNode> getQNameReferenceNode() {
            return Optional.ofNullable(this.qualifiedNameReferenceNode);
        }
    }
}
