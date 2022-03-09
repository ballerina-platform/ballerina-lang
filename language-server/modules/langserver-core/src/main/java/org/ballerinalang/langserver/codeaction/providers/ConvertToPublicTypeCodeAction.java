/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.util.definition.DefinitionUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action to make type public.
 *
 * @since 2.0.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToPublicTypeCodeAction extends AbstractCodeActionProvider {
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!diagnostic.diagnosticInfo().code().equals("BCE2038")) {
            return Collections.emptyList();
        }
        Range range = new Range(CommonUtil.toPosition(diagnostic.location().lineRange().startLine()),
                CommonUtil.toPosition(diagnostic.location().lineRange().endLine()));
        if (context.currentSyntaxTree().isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }
        NonTerminalNode nonTerminalNode = CommonUtil.findNode(range, context.currentSyntaxTree().get());
        Optional<Symbol> symbol = context.currentSemanticModel().get().symbol(nonTerminalNode);
        if (symbol.isEmpty() || symbol.get().getModule().isEmpty()) {
            return Collections.emptyList();
        }

        ModuleID moduleID = symbol.get().getModule().get().id();
        String orgName = moduleID.orgName();
        String moduleName = moduleID.moduleName();
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<Path> filePath = DefinitionUtil.getFilePathForDependency(orgName, moduleName, project.get(),
                symbol.get(), context);
        if (filePath.isEmpty() || context.workspace().syntaxTree(filePath.get()).isEmpty()) {
            return Collections.emptyList();
        }
        URI uri = filePath.get().toUri();

        Optional<NonTerminalNode> node = CommonUtil.findNode(symbol.get(),
                context.workspace().syntaxTree(filePath.get()).get());
        if (node.isEmpty()) {
            return Collections.emptyList();
        }
        Position startPosition = CommonUtil.toPosition(node.get().lineRange().startLine());
        Range recordRange = new Range(startPosition, startPosition);

        String editText = "public ";
        TextEdit textEdit = new TextEdit(recordRange, editText);
        List<TextEdit> editList = List.of(textEdit);
        String recordToPublic = String.format(CommandConstants.MAKE_TYPE_PUBLIC, symbol.get().getName().orElse(""));
        return List.of(createQuickFixCodeAction(recordToPublic, editList, uri.toString()));
    }

    @Override
    public int priority() {
        return super.priority();
    }

    @Override
    public String getName() {
        return "MakePublicCodeAction";
    }
}
