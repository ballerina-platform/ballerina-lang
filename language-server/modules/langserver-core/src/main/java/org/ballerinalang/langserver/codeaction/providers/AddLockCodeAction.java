/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
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
 * A code action to wrap an isolated variable with a lock.
 *
 * @since 2201.9.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddLockCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Add lock";
    private static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE3957", "BCE3962");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        // Determine the enclosing statement node of the isolated variable
        Optional<StatementNode> statementNode = getMatchingStatementNode(positionDetails.matchedNode());
        if (statementNode.isEmpty()) {
            return Collections.emptyList();
        }

        // Check if there are multiple isolated variables within a single statement
        List<Diagnostic> diagnostics = context.diagnostics(context.filePath());
        if (diagnostics.size() > 1 && hasMultipleIsolationVars(statementNode.get(), diagnostic, diagnostics)) {
            return Collections.emptyList();
        }

        // Generate and return the text edit for the lock statement
        TextEdit surroundWithLockEditText = getTextEdit(statementNode.get());
        return Collections.singletonList(CodeActionUtil.createCodeAction(
                CommandConstants.SURROUND_WITH_LOCK,
                List.of(surroundWithLockEditText),
                context.fileUri(),
                CodeActionKind.QuickFix)
        );
    }

    private static Optional<StatementNode> getMatchingStatementNode(Node matchedNode) {
        Node parentNode = matchedNode.parent();
        while (parentNode != null && !(parentNode instanceof StatementNode)) {
            // Lock statement does not support async calls
            if (parentNode.kind() == SyntaxKind.START_ACTION) {
                return Optional.empty();
            }
            parentNode = parentNode.parent();
        }

        // Check if the lock statement contains any async calls
        if (parentNode != null && parentNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            SyntaxKind kind = ((AssignmentStatementNode) parentNode).expression().kind();
            if (kind == SyntaxKind.RECEIVE_ACTION || kind == SyntaxKind.START_ACTION) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable((StatementNode) parentNode);
    }

    private static TextEdit getTextEdit(Node node) {
        TextDocument textDocument = node.syntaxTree().textDocument();
        TextRange textRange = node.textRangeWithMinutiae();
        LinePosition startLinePosition = textDocument.linePositionFrom(textRange.startOffset());
        LinePosition endLinePosition = textDocument.linePositionFrom(textRange.endOffset());
        Position startPosition = PositionUtil.toPosition(startLinePosition);
        Position endPosition = PositionUtil.toPosition(endLinePosition);

        String spaces = " ".repeat(node.lineRange().startLine().offset());
        String statement = node.toSourceCode();
        String indentedStatement = statement.substring(0, statement.length() - 1).replace("\n", "\n\t") + "\n";

        String editText =
                spaces + "lock {" + CommonUtil.LINE_SEPARATOR + "\t" + indentedStatement + spaces + "}" + "\n";
        return new TextEdit(new Range(startPosition, endPosition), editText);
    }

    private static boolean hasMultipleIsolationVars(StatementNode statementNode, Diagnostic currentDiagnostic,
                                                    List<Diagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic -> !currentDiagnostic.equals(diagnostic) &&
                DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) &&
                PositionUtil.isWithinLineRange(diagnostic.location().lineRange(), statementNode.lineRange()));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
