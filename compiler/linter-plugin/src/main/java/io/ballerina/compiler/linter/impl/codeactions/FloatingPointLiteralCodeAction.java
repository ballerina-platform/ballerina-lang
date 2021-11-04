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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.linter.impl.codeactions.Constants.BCE_MISSING_DIGIT_AFTER_DOT;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.BCE_MISSING_HEX_DIGIT_AFTER_DOT;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.CA_FLOATING_POINT;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.DOT;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.DOT_AND_ZERO;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.LINE_RANGE;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.REPLACE_WITH;
import static io.ballerina.compiler.linter.impl.codeactions.Constants.VALUE;

/**
 * Code action to handle trailing `.` in floating point literal.
 * <p>
 * Ballerina spec 2021R2 states that, floating point literal must not end with a `.`. Following Ballerina code shows
 * the problematic case.
 * <code>
 * public function main() {
 * decimal d1 = 2.;    // Should be 2.0
 * decimal d2 = 2.d;   // Should be 2.0d
 * decimal d3 = 2.D;   // Should be 2.D
 * decimal d4 = 2.e12; // Should be 2.0e12
 * float f1 = 2.f;     // Should be 2.0f
 * float f2 = 2.F;     // Should be 2.0F
 * float f3 = 0x1A.;   // Should be 0x1A.0
 * float f4 = 0x1A.p4; // Should be 0x1A.0p4
 * }
 * </code>
 *
 * @since 2.0.0
 */
public class FloatingPointLiteralCodeAction extends LinterCodeAction {

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(BCE_MISSING_HEX_DIGIT_AFTER_DOT, BCE_MISSING_DIGIT_AFTER_DOT);
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {

        final NonTerminalNode node = getNodeFromDiagnostics(context);

        SyntaxKind kind = node.kind();
        final BasicLiteralNode literalNode;
        if (kind == SyntaxKind.NUMERIC_LITERAL) {
            literalNode = (BasicLiteralNode) node;
        } else {
            return Optional.empty();    // Should not reach here.
        }

        final String text = literalNode.literalToken().text();
        final String newLiteral = text.replace(DOT, DOT_AND_ZERO);

        List<CodeActionArgument> args = List.of(CodeActionArgument.from(LINE_RANGE, node.lineRange()),
                CodeActionArgument.from(VALUE, newLiteral));
        return Optional.of(CodeActionInfo.from(String.format(REPLACE_WITH, newLiteral), args));
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
        return CA_FLOATING_POINT;
    }
}
