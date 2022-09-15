/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
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
import java.util.stream.Collectors;

/**
 * Code action to add conditional default value.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddConditionalDefaultValueCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Add conditional default value";
    public static final String DIAGNOSTIC_CODES = "BCE2066";

    @Override
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return context.currentSemanticModel().isPresent() &&
                DIAGNOSTIC_CODES.equals(diagnostic.diagnosticInfo().code()) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<TypeSymbol> actualTypeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        Optional<TypeSymbol> expectedTypeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED_SYMBOL_INDEX);

        if (expectedTypeSymbol.isEmpty() || actualTypeSymbol.isEmpty()
                || !isOptionalType(actualTypeSymbol.get(), context)) {
            return Collections.emptyList();
        }
        Optional<TypeSymbol> typeWithoutOptional = getTypeWithoutOptional(actualTypeSymbol.get(), context);
        if (typeWithoutOptional.isEmpty() || !typeWithoutOptional.get().subtypeOf(expectedTypeSymbol.get())) {
            return Collections.emptyList();
        }
        Optional<String> defaultValue = DefaultValueGenerationUtil.getDefaultValueForType(typeWithoutOptional.get());
        if (defaultValue.isEmpty()) {
            return Collections.emptyList();
        }
        Node matchedNode = positionDetails.matchedNode();
        Position endPosition = PositionUtil.toPosition(matchedNode.lineRange().endLine());
        String editText = " ?: " + defaultValue.get();
        TextEdit textEdit = new TextEdit(new Range(endPosition, endPosition), editText);
        return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.ADD_CONDITIONAL_DEFAULT,
                List.of(textEdit), context.fileUri(), CodeActionKind.QuickFix));
    }

    private Optional<TypeSymbol> getTypeWithoutOptional(TypeSymbol actualType, CodeActionContext context) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (actualType.typeKind() != TypeDescKind.UNION || semanticModel.isEmpty()) {
            return Optional.empty();
        }
        List<TypeSymbol> memberTypes = ((UnionTypeSymbol) actualType).memberTypeDescriptors().stream()
                .filter(typeSymbol -> typeSymbol.typeKind() != TypeDescKind.NIL).collect(Collectors.toList());
        UnionTypeSymbol unionType = semanticModel.get().types().builder().UNION_TYPE
                .withMemberTypes(memberTypes.toArray(TypeSymbol[]::new)).build();
        return Optional.of(unionType);
    }

    private boolean isOptionalType(TypeSymbol typeSymbol, CodeActionContext context) {
        TypeSymbol nilType = context.currentSemanticModel().get().types().NIL;
        return nilType.subtypeOf(typeSymbol);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
