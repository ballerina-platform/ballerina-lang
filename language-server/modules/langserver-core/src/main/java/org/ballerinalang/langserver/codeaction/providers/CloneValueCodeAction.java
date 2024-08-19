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

import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.tools.diagnostics.Diagnostic;
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
 * Code action to clone the value or clone it as a readonly value.
 *
 * @since 2201.10.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CloneValueCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Clone the value";
    private static final String DIAGNOSTIC_CODE_3959 = "BCE3959";
    private static final Set<String> DIAGNOSTIC_CODES = Set.of(DIAGNOSTIC_CODE_3959, "BCE3960");
    private static final String CLONE_METHOD = ".clone()";
    private static final String CLONE_READONLY_METHOD = ".cloneReadOnly()";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        NonTerminalNode matchedNode = positionDetails.matchedNode();

        // Check if there are multiple diagnostics of the considered category
        if (CommonUtil.hasMultipleDiagnostics(context, matchedNode, diagnostic, DIAGNOSTIC_CODES,
                DIAGNOSTIC_CODE_3959)) {
            return Collections.emptyList();
        }

        // Obtain the type symbol of the matched node.
        Optional<TypeSymbol> optTypeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(positionDetails.matchedNode()));
        if (optTypeSymbol.isEmpty()) {
            assert false : "The diagnostic cannot be produced for a node without a type symbol.";
            return Collections.emptyList();
        }
        TypeSymbol typeSymbol = optTypeSymbol.get();

        // The code action is only valid for structured types.
        if (!isStructuredType(typeSymbol)) {
            return Collections.emptyList();
        }

        // Generate both code actions for cloning and cloning as a readonly value.
        Position position = PositionUtil.toPosition(matchedNode.lineRange().endLine());
        Range range = new Range(position, position);
        String fileUri = context.fileUri();
        return List.of(getCodeAction(CommandConstants.CLONE_VALUE, CLONE_METHOD, range, fileUri),
                getCodeAction(CommandConstants.CLONE_READONLY_VALUE, CLONE_READONLY_METHOD, range, fileUri));
    }

    private static CodeAction getCodeAction(String commandName, String methodName, Range range, String fileUri) {
        return CodeActionUtil.createCodeAction(
                commandName,
                Collections.singletonList(new TextEdit(range, methodName)),
                fileUri,
                CodeActionKind.QuickFix);
    }

    private static boolean isStructuredType(TypeSymbol typeSymbol) {
        return switch (typeSymbol.typeKind()) {
            case RECORD, MAP, ARRAY, TABLE, TUPLE, JSON, ANYDATA, UNION -> true;
            case TYPE_REFERENCE -> isStructuredType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            default -> false;
        };
    }

    @Override
    public String getName() {
        return NAME;
    }
}
