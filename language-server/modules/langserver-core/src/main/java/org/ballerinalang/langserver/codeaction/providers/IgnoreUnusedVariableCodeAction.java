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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.references.ReferencesUtil;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code action to ignore an unused variable.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class IgnoreUnusedVariableCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "IGNORE_VARIABLE";

    /**
     * Interested diagnostic codes of this code action.
     */
    private static final Set<String> DIAGNOSTIC_CODES = Set.of(
            DiagnosticWarningCode.UNUSED_LOCAL_VARIABLE.diagnosticId(),
            DiagnosticErrorCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT.diagnosticId()
    );

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        Optional<NonTerminalNode> nonTerminalNode = context.currentSyntaxTree()
                .flatMap(syntaxTree -> Optional.ofNullable(CommonUtil.findNode(range, syntaxTree)));

        if (nonTerminalNode.isEmpty()) {
            return Collections.emptyList();
        }

        NonTerminalNode node = nonTerminalNode.get();
        if (node.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            node = node.parent();
        }

        BindingPatternNode bindingPatternNode = null;
        if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode varDeclNode = (VariableDeclarationNode) node;
            if (varDeclNode.initializer().isPresent()) {
                // If no initializer is present, we can't ignore the variable
                bindingPatternNode = varDeclNode.typedBindingPattern().bindingPattern();
            }
        } else if (node.kind() == SyntaxKind.TYPED_BINDING_PATTERN) {
            TypedBindingPatternNode typedBindingPatternNode = (TypedBindingPatternNode) node;
            bindingPatternNode = typedBindingPatternNode.bindingPattern();
        } else if (node.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN ||
                node.kind() == SyntaxKind.FIELD_BINDING_PATTERN ||
                node.kind() == SyntaxKind.MAPPING_BINDING_PATTERN ||
                node.kind() == SyntaxKind.LIST_BINDING_PATTERN) {
            bindingPatternNode = (BindingPatternNode) node;
        } else if (node.kind() == SyntaxKind.LET_VAR_DECL) {
            bindingPatternNode = ((LetVariableDeclarationNode) node).typedBindingPattern().bindingPattern();
        }

        if (bindingPatternNode == null) {
            return Collections.emptyList();
        }

        // Only subtypes of any can be ignored.
        if (bindingPatternNode.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN 
                && !isSubTypeOfAny(bindingPatternNode, context)) {
            return Collections.emptyList();
        }

        // If it's a variable, need to check for references
        if (bindingPatternNode.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN) {
            BindingPatternNode finalBindingPatternNode = bindingPatternNode;
            Optional<Integer> refCount = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.symbol(finalBindingPatternNode))
                    .flatMap(symbol -> context.workspace().project(context.filePath())
                            .map(project -> ReferencesUtil.getReferences(project, symbol)))
                    .map(modRefMap -> modRefMap.values().stream().map(List::size).reduce(0, Integer::sum));

            // If more than 1 reference, we don't show the codeaction
            if (refCount.isPresent() && refCount.get() > 1) {
                return Collections.emptyList();
            }
        }

        TextEdit textEdit = null;
        if (bindingPatternNode.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN ||
                bindingPatternNode.kind() == SyntaxKind.MAPPING_BINDING_PATTERN ||
                bindingPatternNode.kind() == SyntaxKind.LIST_BINDING_PATTERN) {
            Range editRange = PositionUtil.toRange(bindingPatternNode.lineRange());
            textEdit = new TextEdit(editRange, "_");
        } else if (bindingPatternNode.kind() == SyntaxKind.FIELD_BINDING_PATTERN) {
            if (bindingPatternNode instanceof FieldBindingPatternVarnameNode fieldBindingPattern) {
                Position position = PositionUtil.toPosition(fieldBindingPattern.variableName().lineRange().endLine());
                Range editRange = new Range(position, position);
                textEdit = new TextEdit(editRange, ": _");
            }
        }

        if (textEdit == null) {
            return Collections.emptyList();
        }

        return List.of(CodeActionUtil.createCodeAction(CommandConstants.IGNORE_UNUSED_VAR_TITLE,
                List.of(textEdit), context.fileUri(), CodeActionKind.QuickFix));
    }

    /**
     * Check whether the given binding pattern node is a subtype of any.
     *
     * @param bindingPatternNode binding pattern node
     * @param context            code action context
     * @return {@code true} if the given binding pattern node is a subtype of any
     */
    private boolean isSubTypeOfAny(BindingPatternNode bindingPatternNode, CodeActionContext context) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return false;
        }

        Optional<TypeSymbol> typeSymbol = semanticModel.get().symbol(bindingPatternNode)
                .flatMap(SymbolUtil::getTypeDescriptor);

        if (typeSymbol.isEmpty()) {
            return false;
        }

        return typeSymbol.get().subtypeOf(semanticModel.get().types().ANY);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
