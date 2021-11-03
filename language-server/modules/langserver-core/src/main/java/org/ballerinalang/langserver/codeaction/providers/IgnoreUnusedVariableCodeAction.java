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

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.eclipse.lsp4j.CodeAction;
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
public class IgnoreUnusedVariableCodeAction extends AbstractCodeActionProvider {

    private static final String NAME = "IGNORE_VARIABLE";

    /** Interested diagnostic codes of this code action. */
    private static final Set<String> DIAGNOSTIC_CODES = Set.of(
            DiagnosticWarningCode.UNUSED_LOCAL_VARIABLE.diagnosticId(),
            DiagnosticErrorCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT.diagnosticId()
    );

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        Optional<NonTerminalNode> nonTerminalNode = context.currentSyntaxTree()
                .flatMap(syntaxTree -> Optional.ofNullable(CommonUtil.findNode(range, syntaxTree)));

        if (nonTerminalNode.isEmpty()) {
            return Collections.emptyList();
        }

        NonTerminalNode node = nonTerminalNode.get();
        if (node.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            node = node.parent();
        }

        BindingPatternNode bindingPatternNode;
        if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode varDeclNode = (VariableDeclarationNode) node;
            bindingPatternNode = varDeclNode.typedBindingPattern().bindingPattern();
        } else if (node.kind() == SyntaxKind.TYPED_BINDING_PATTERN) {
            TypedBindingPatternNode typedBindingPatternNode = (TypedBindingPatternNode) node;
            bindingPatternNode = typedBindingPatternNode.bindingPattern();
        } else if (node.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN) {
            bindingPatternNode = (BindingPatternNode) node;
        } else if (node.kind() == SyntaxKind.FIELD_BINDING_PATTERN) {
            bindingPatternNode = (BindingPatternNode) node;
        } else if (node.kind() == SyntaxKind.MAPPING_BINDING_PATTERN) {
            bindingPatternNode = (BindingPatternNode) node;
        } else if (node.kind() == SyntaxKind.LIST_BINDING_PATTERN) {
            bindingPatternNode = (BindingPatternNode) node;
        } else {
            return Collections.emptyList();
        }

        TextEdit textEdit = null;
        if (bindingPatternNode.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN ||
                bindingPatternNode.kind() == SyntaxKind.MAPPING_BINDING_PATTERN ||
                bindingPatternNode.kind() == SyntaxKind.LIST_BINDING_PATTERN) {
            Range editRange = CommonUtil.toRange(bindingPatternNode.lineRange());
            textEdit = new TextEdit(editRange, "_");
        } else if (bindingPatternNode.kind() == SyntaxKind.FIELD_BINDING_PATTERN) {
            if (bindingPatternNode instanceof FieldBindingPatternVarnameNode) {
                FieldBindingPatternVarnameNode fieldBindingPattern =
                        (FieldBindingPatternVarnameNode) bindingPatternNode;
                Position position = CommonUtil.toPosition(fieldBindingPattern.variableName().lineRange().endLine());
                Range editRange = new Range(position, position);
                textEdit = new TextEdit(editRange, ": _");
            }
        }

        if (textEdit == null) {
            return Collections.emptyList();
        }

        return List.of(createQuickFixCodeAction(CommandConstants.IGNORE_UNUSED_VAR_TITLE,
                List.of(textEdit), context.fileUri()));
    }

    @Override
    public String getName() {
        return null;
    }
}
