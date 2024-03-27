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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for making a variable immutable. This will ensure that the given variable is both final and readonly.
 *
 * @since 2201.9.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MakeVariableImmutableCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Make variable immutable";
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
        NonTerminalNode cursorNode = positionDetails.matchedNode();

        // The current implementation of the CA only supports object fields
        if (cursorNode.kind() != SyntaxKind.OBJECT_FIELD) {
            assert false : "This line is unreachable as the diagnostic is only generated for an object field.";
            return Collections.emptyList();
        }

        ObjectFieldNode objectFieldNode = (ObjectFieldNode) cursorNode;
        Node typeNode = objectFieldNode.typeName();
        List<TextEdit> textEdits = new ArrayList<>();

        // Check if the type is final
        boolean isFinal = objectFieldNode.qualifierList().stream()
                .anyMatch(token -> token.kind().equals(SyntaxKind.FINAL_KEYWORD));
        if (!isFinal) {
            textEdits.add(getFinalTextEdit(typeNode));
        }

        // Check if the type is readonly
        TypeSymbol typeSymbol, readonlyType;
        try {
            SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
            readonlyType = semanticModel.types().READONLY;
            Symbol symbol = semanticModel.symbol(cursorNode).orElseThrow();
            typeSymbol = getTypeSymbol(symbol).orElseThrow();
        } catch (RuntimeException e) {
            assert false : "This line is unreachable because the semantic model cannot be empty, and the type " +
                    "symbol does not contain errors.";
            return Collections.emptyList();
        }
        boolean isReadonly = typeSymbol.subtypeOf(readonlyType);
        if (!isReadonly) {
            textEdits.addAll(getReadonlyTextEdits(typeNode, typeSymbol.typeKind() == TypeDescKind.UNION));
        }

        // Generate and return the code action
        return Collections.singletonList(CodeActionUtil.createCodeAction(
                String.format(CommandConstants.MAKE_VARIABLE_IMMUTABLE, getTitleText(isFinal, isReadonly)),
                textEdits,
                context.fileUri(),
                CodeActionKind.QuickFix));
    }

    private static Optional<TypeSymbol> getTypeSymbol(Symbol symbol) {
        if (symbol.kind() == SymbolKind.CLASS_FIELD) {
            return Optional.of(((ClassFieldSymbol) symbol).typeDescriptor());
        }
        assert false : "Unconsidered symbol type found: " + symbol.kind();
        return Optional.empty();
    }

    private static TextEdit getFinalTextEdit(Node typeNode) {
        LinePosition linePosition = typeNode.lineRange().startLine();
        Position position = PositionUtil.toPosition(linePosition);
        String editText = SyntaxKind.FINAL_KEYWORD.stringValue() + " ";
        return new TextEdit(new Range(position, position), editText);
    }

    private static List<TextEdit> getReadonlyTextEdits(Node typeNode, boolean isUnion) {
        LinePosition startLinePosition = typeNode.lineRange().startLine();
        LinePosition endLinePosition = typeNode.lineRange().endLine();
        List<TextEdit> textEdits = new ArrayList<>();

        if (isUnion) {
            Position startPosition = PositionUtil.toPosition(startLinePosition);
            TextEdit startTextEdit = new TextEdit(new Range(startPosition, startPosition),
                    SyntaxKind.OPEN_PAREN_TOKEN.stringValue());
            textEdits.add(startTextEdit);
        }

        Position endPosition = PositionUtil.toPosition(endLinePosition);
        String editText = (isUnion ? SyntaxKind.CLOSE_PAREN_TOKEN.stringValue() : "") + " & " +
                SyntaxKind.READONLY_KEYWORD.stringValue();
        TextEdit endTextEdit = new TextEdit(new Range(endPosition, endPosition), editText);
        textEdits.add(endTextEdit);

        return textEdits;
    }

    private static String getTitleText(boolean isFinal, boolean isReadonly) {
        StringBuilder result = new StringBuilder();

        if (!isFinal) {
            result.append("'").append(SyntaxKind.FINAL_KEYWORD.stringValue()).append("'");
        }

        if (!isReadonly) {
            if (result.length() > 0) {
                result.append(" and ");
            }
            result.append("'").append(SyntaxKind.READONLY_KEYWORD.stringValue()).append("'");
        }

        return result.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
