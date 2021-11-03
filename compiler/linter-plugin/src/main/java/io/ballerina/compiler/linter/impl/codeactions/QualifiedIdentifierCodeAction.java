/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.linter.impl.codeactions;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.XMLQualifiedNameNode;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.linter.impl.codeactions.Constants.BCE_INTERVENING_WS;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.BCE_INVALID_WS;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.CA_QUALIFIED_IDENTIFIER;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.LINE_RANGE;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.REPLACE_WITH;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.VALUE;

/**
 * Code action to handle whitespaces in Qualified-Identifiers.
 * <p>
 * Qualified identifier must not have intervening white spaces, in 2021R2 Spec. This Code action will help users
 * to migrate such code.
 *
 * <code>
 * io :print("hello");  // Now Compiler time error.
 * </code>
 *
 * @since 2.0.0
 */
public class QualifiedIdentifierCodeAction extends LinterCodeAction {

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(BCE_INVALID_WS, BCE_INTERVENING_WS);
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {

        final NonTerminalNode node = getNodeFromDiagnostics(context);

        final String updatedText;
        SyntaxKind kind = node.kind();
        if (kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            final QualifiedNameReferenceNode qualifiedNameReferenceNode = (QualifiedNameReferenceNode) node;
            updatedText = qualifiedNameReferenceNode.modulePrefix().toSourceCode().stripTrailing()
                    + qualifiedNameReferenceNode.colon().toSourceCode().strip()
                    + qualifiedNameReferenceNode.identifier().toSourceCode().stripLeading();
        } else if (kind == SyntaxKind.XML_QUALIFIED_NAME) {
            final XMLQualifiedNameNode xmlQualifiedNameNode = (XMLQualifiedNameNode) node;
            updatedText = xmlQualifiedNameNode.prefix().toSourceCode().stripTrailing()
                    + xmlQualifiedNameNode.colon().toSourceCode().strip()
                    + xmlQualifiedNameNode.name().toSourceCode().stripLeading();
        } else {
            return Optional.empty();    // Skip code action.
        }

        List<CodeActionArgument> args = List.of(CodeActionArgument.from(LINE_RANGE, node.lineRange()),
                CodeActionArgument.from(VALUE, updatedText));
        return Optional.of(CodeActionInfo.from(String.format(REPLACE_WITH, updatedText), args));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        LineRange lineRange = context.arguments().get(0).valueAs(LineRange.class);
        String updatedText = context.arguments().get(1).valueAs(String.class);

        DocumentEdit documentEdit = getDocumentEdit(context, lineRange, updatedText);
        return List.of(documentEdit);
    }

    @Override
    public String name() {
        return CA_QUALIFIED_IDENTIFIER;
    }
}
