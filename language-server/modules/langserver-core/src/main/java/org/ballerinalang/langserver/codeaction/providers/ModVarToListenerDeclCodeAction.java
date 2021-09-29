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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code action for changing a module var declaration statement to listener declaration.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ModVarToListenerDeclCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Module var to listener declaration";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(DiagnosticErrorCode.INVALID_LISTENER_ATTACHMENT.diagnosticId()
                .equals(diagnostic.diagnosticInfo().code()))) {
            return Collections.emptyList();
        }

        Node matchedNode = positionDetails.matchedNode();
        if (matchedNode == null) {
            return Collections.emptyList();
        }
        Optional<Pair<CaptureBindingPatternNode, String>> nodeUriPair =
                findCaptureBindingPattern(matchedNode, context);
        if (nodeUriPair.isEmpty() || nodeUriPair.get().getLeft().parent().kind() != SyntaxKind.TYPED_BINDING_PATTERN) {
            return Collections.emptyList();
        }
        TypedBindingPatternNode typedBindingPatternNode =
                (TypedBindingPatternNode) nodeUriPair.get().getLeft().parent();
        List<CodeAction> actions = new ArrayList<>();
        List<TextEdit> textEdits = new ArrayList<>();
        Position pos = CommonUtil.toRange(typedBindingPatternNode.lineRange()).getStart();
        Position insertPos = new Position(pos.getLine(), pos.getCharacter());
        textEdits.add(new TextEdit(new Range(insertPos, insertPos),
                SyntaxKind.LISTENER_KEYWORD.stringValue().trim() + " "));
        String commandTitle = String.format(CommandConstants.CONVERT_MODULE_VAR_TO_LISTENER_DECLARATION,
                matchedNode.toSourceCode().trim());
        actions.add(createQuickFixCodeAction(commandTitle, textEdits,
                nodeUriPair.get().getRight()));
        return actions;
    }

    private Optional<Pair<CaptureBindingPatternNode, String>> findCaptureBindingPattern(Node matchedNode,
                                                                                        CodeActionContext context) {
        Optional<Symbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(matchedNode));
        if (symbol.isEmpty() || context.currentSyntaxTree().isEmpty() 
                || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        Optional<NonTerminalNode> foundNode;
        String uri;
        if (matchedNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            //Todo: we need a proper API to get the syntax tree node. 
            Optional<Project> project = context.workspace().project(context.filePath());
            Optional<Location> location = symbol.get().getLocation();
            if (location.isEmpty() || project.isEmpty() ||
                    project.get().kind() != ProjectKind.BUILD_PROJECT || symbol.get().getModule().isEmpty()) {
                return Optional.empty();
            }
            Path filePath = project.get().sourceRoot()
                    .resolve("modules").resolve(symbol.get().getModule().get().id().modulePrefix())
                    .resolve(location.get().lineRange().filePath());
            Optional<SyntaxTree> syntaxTree = context.workspace().syntaxTree(filePath);
            if (syntaxTree.isEmpty()) {
                return Optional.empty();
            }
            foundNode = CommonUtil.findNode(symbol.get(), syntaxTree.get());
            uri = filePath.toUri().toString();
        } else {
            foundNode = CommonUtil.findNode(symbol.get(), context.currentSyntaxTree().get());
            uri = context.fileUri();
        }
        if (foundNode.isEmpty() || foundNode.get().kind() != SyntaxKind.CAPTURE_BINDING_PATTERN) {
            return Optional.empty();
        }
        return Optional.of(Pair.of((CaptureBindingPatternNode) foundNode.get(), uri));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
