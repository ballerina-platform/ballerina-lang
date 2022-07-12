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
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
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
 * Code Action for ignore variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class IgnoreReturnCodeAction extends CreateVariableCodeAction {

    public static final String NAME = "Ignore Return Type";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, 
                            CodeActionContext context) {
        return  diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED) &&
                CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        Optional<TypeSymbol> typeDescriptor = getExpectedTypeSymbol(positionDetails);
        if (typeDescriptor.isEmpty()) {
            return Collections.emptyList();
        }
        String uri = context.fileUri();
        Position pos = PositionUtil.toRange(diagnostic.location().lineRange()).getStart();
        // Add ignore return value code action
        if (!hasErrorType(typeDescriptor.get())) {
            String commandTitle = CommandConstants.IGNORE_RETURN_TITLE;
            return Collections.singletonList(
                    createCodeAction(commandTitle, getIgnoreCodeActionEdits(pos), uri, CodeActionKind.QuickFix)
            );
        }
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return NAME;
    }

    private boolean hasErrorType(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() == TypeDescKind.ERROR) {
            return true;
        } else if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionType = (UnionTypeSymbol) typeSymbol;
            return CodeActionUtil.hasErrorMemberType(unionType);
        }
        return false;
    }

    private static List<TextEdit> getIgnoreCodeActionEdits(Position position) {
        String editText = "_ = ";
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }
}
