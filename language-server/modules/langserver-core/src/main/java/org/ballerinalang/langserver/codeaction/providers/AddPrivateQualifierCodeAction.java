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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
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

/**
 * Code Action for adding the private visibility qualifier ƒor an object field.
 *
 * @since 2201.9.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddPrivateQualifierCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Add private visibility qualifier";
    private static final String DIAGNOSTIC_CODE = "BCE3956";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODE.equals(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Node cursorNode = positionDetails.matchedNode();
        if (cursorNode.kind() != SyntaxKind.OBJECT_FIELD) {
            assert false : "This line is unreachable as the diagnostic is only generated for an object field.";
            return Collections.emptyList();
        }

        return Collections.singletonList(CodeActionUtil.createCodeAction(
                CommandConstants.ADD_PRIVATE_QUALIFIER,
                List.of(getTextEdit((ObjectFieldNode) cursorNode)),
                context.fileUri(),
                CodeActionKind.QuickFix
        ));
    }

    private static TextEdit getTextEdit(ObjectFieldNode node) {
        String privateKeyword = SyntaxKind.PRIVATE_KEYWORD.stringValue();

        // Get the line range of the existing visibility qualifier
        Optional<Token> visibilityQualifier = node.visibilityQualifier();
        if (visibilityQualifier.isPresent()) {
            return new TextEdit(PositionUtil.toRange(visibilityQualifier.get().lineRange()), privateKeyword);
        }
        privateKeyword += " ";

        // Get the start position of the qualifier list
        NodeList<Token> qualifiers = node.qualifierList();
        if (qualifiers.size() > 0) {
            Position position = PositionUtil.toPosition(qualifiers.get(0).lineRange().startLine());
            return new TextEdit(new Range(position, position), privateKeyword);
        }

        // Get the start position of the type name
        Position position = PositionUtil.toPosition(node.typeName().lineRange().startLine());
        return new TextEdit(new Range(position, position), privateKeyword);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
