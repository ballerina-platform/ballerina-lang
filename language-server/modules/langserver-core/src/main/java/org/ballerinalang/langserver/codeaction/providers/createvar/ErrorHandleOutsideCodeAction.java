/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.createvar;

import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.getAddCheckTextEdits;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorHandleOutsideCodeAction extends CreateVariableCodeAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 998;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        String uri = context.fileUri();
        if (!(diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }
        TypeSymbol typeSymbol = positionDetails.matchedExprType();
        if (typeSymbol == null || typeSymbol.typeKind() != TypeDescKind.UNION) {
            return Collections.emptyList();
        }
        UnionTypeSymbol unionTypeDesc = (UnionTypeSymbol) typeSymbol;

        boolean hasErrorMemberType = unionTypeDesc.memberTypeDescriptors().stream()
                .anyMatch(member -> member.typeKind() == TypeDescKind.ERROR);
        long nonErrorNonNilMemberCount = unionTypeDesc.memberTypeDescriptors().stream()
                .filter(member -> member.typeKind() != TypeDescKind.ERROR && member.typeKind() != TypeDescKind.NIL)
                .count();
        if (!hasErrorMemberType || nonErrorNonNilMemberCount == 0) {
            return Collections.emptyList();
        }
        List<TextEdit> edits = new ArrayList<>();
        edits.addAll(getModifiedCreateVarTextEdits(diagnostic, unionTypeDesc, positionDetails, context));
        edits.addAll(getAddCheckTextEdits(CommonUtil.toRange(diagnostic.location().lineRange()).getStart(),
                                          positionDetails.matchedNode(), context));

        String commandTitle = CommandConstants.CREATE_VAR_ADD_CHECK_TITLE;
        return Collections.singletonList(AbstractCodeActionProvider.createQuickFixCodeAction(commandTitle, edits, uri));
    }

    private List<TextEdit> getModifiedCreateVarTextEdits(Diagnostic diagnostic,
                                                         UnionTypeSymbol unionTypeDesc,
                                                         DiagBasedPositionDetails positionDetails,
                                                         CodeActionContext context) {
        List<TextEdit> edits = new ArrayList<>();

        // Add create variable edits
        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, context);

        // Change and add type text edit
        String typeWithError = createVarTextEdits.types.get(0);
        String typeWithoutError = unionTypeDesc.memberTypeDescriptors().stream()
                .filter(member -> member.typeKind() != TypeDescKind.ERROR)
                .map(typeDesc -> CodeActionUtil.getPossibleType(typeDesc, edits, context).orElseThrow())
                .collect(Collectors.joining("|"));

        TextEdit textEdit = createVarTextEdits.edits.get(0);
        textEdit.setNewText(typeWithoutError + textEdit.getNewText().substring(typeWithError.length()));
        edits.add(textEdit);
        edits.addAll(createVarTextEdits.imports);
        return edits;
    }
}
