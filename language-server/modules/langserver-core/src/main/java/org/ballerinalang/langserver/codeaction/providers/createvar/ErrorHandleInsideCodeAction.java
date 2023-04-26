/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for type guard variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorHandleInsideCodeAction extends CreateVariableCodeAction {

    public static final String NAME = "Error Handle Inside";

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {

        return 997;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {

        return diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        Optional<TypeSymbol> typeDescriptor = getExpectedTypeSymbol(positionDetails);
        if (typeDescriptor.isEmpty() || typeDescriptor.get().typeKind() != TypeDescKind.UNION
                || isUnionCompErrorTyped((UnionTypeSymbol) typeDescriptor.get())) {
            return Collections.emptyList();
        }

        String uri = context.fileUri();
        UnionTypeSymbol unionType = (UnionTypeSymbol) typeDescriptor.get();

        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, typeDescriptor.get(),
                context, new ImportsAcceptor(context));

        // Add type guard code action
        String commandTitle = CommandConstants.CREATE_VAR_TYPE_GUARD_TITLE;
        List<TextEdit> edits = new ArrayList<>();
        edits.add(createVarTextEdits.edits.get(0));
        edits.addAll(CodeActionUtil.getTypeGuardCodeActionEdits(createVarTextEdits.name, range, unionType, context));

        // Add all the import text edits excluding duplicates
        createVarTextEdits.imports.stream().filter(edit -> !edits.contains(edit)).forEach(edits::add);

        CodeAction codeAction = CodeActionUtil.createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix);
        addRenamePopup(context, edits, createVarTextEdits.edits.get(0), codeAction,
                createVarTextEdits.renamePositions.get(0), createVarTextEdits.varRenamePosition.get(0),
                createVarTextEdits.imports.size());
        return Collections.singletonList(codeAction);
    }

    @Override
    public String getName() {

        return NAME;
    }
}
