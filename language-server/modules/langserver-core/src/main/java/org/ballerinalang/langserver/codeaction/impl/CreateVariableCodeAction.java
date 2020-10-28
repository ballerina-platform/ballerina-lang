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
package org.ballerinalang.langserver.codeaction.impl;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction.getPossibleTypes;
import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
public class CreateVariableCodeAction implements DiagBasedCodeAction {
    private final Symbol scopedSymbol;
    private final BallerinaTypeDescriptor typeDescriptor;

    public CreateVariableCodeAction(BallerinaTypeDescriptor typeDescriptor, Symbol scopedSymbol) {
        this.typeDescriptor = typeDescriptor;
        this.scopedSymbol = scopedSymbol;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position pos = diagnostic.getRange().getStart();

        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        String name = (this.scopedSymbol != null) ? this.scopedSymbol.name() : this.typeDescriptor.signature();
        String varName = CommonUtil.generateVariableName(name, CommonUtil.getAllNameEntries(compilerContext));
        return getCreateVariableCodeActions(context, uri, pos, varName, this.typeDescriptor);
    }

    private static List<CodeAction> getCreateVariableCodeActions(LSContext context, String uri, Position position,
                                                                 String name,
                                                                 BallerinaTypeDescriptor typeDescriptor) {
        List<CodeAction> actions = new ArrayList<>();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        List<TextEdit> importEdits = new ArrayList<>();
        List<String> types = getPossibleTypes(context, typeDescriptor, importEdits, compilerContext);

        for (String type : types) {
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            if (types.size() > 1) {
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }
            Position insertPos = new Position(position.getLine(), position.getCharacter());
            String edit = type + " " + name + " = ";
            List<TextEdit> edits = new ArrayList<>();
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
            edits.addAll(importEdits);
            actions.add(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return actions;
    }
}
