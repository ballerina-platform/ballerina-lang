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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction extends AbstractCodeActionProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 999;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        if (!(diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return actions;
        }
        if (positionDetails.matchedExprType() == null) {
            return actions;
        }

        String uri = context.fileUri();
        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, context);
        List<String> types = createVarTextEdits.types;
        for (int i = 0; i < types.size(); i++) {
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            List<TextEdit> edits = new ArrayList<>();
            edits.add(createVarTextEdits.edits.get(i));
            edits.addAll(createVarTextEdits.imports);
            String type = types.get(i);
            if (createVarTextEdits.types.size() > 1) {
                // When there's multiple types; suffix code actions with `with <type>`
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }
            actions.add(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return actions;
    }

    CreateVariableOut getCreateVariableTextEdits(Range range,
                                                 DiagBasedPositionDetails positionDetails,
                                                 CodeActionContext context) {
        Symbol matchedSymbol = positionDetails.matchedSymbol();
        TypeSymbol typeDescriptor = positionDetails.matchedExprType();

        Position position = CommonUtil.toPosition(positionDetails.matchedNode().lineRange().startLine());
        Set<String> allNameEntries = context.visibleSymbols(position).stream()
                .filter(s -> s.getName().isPresent())
                .map(s -> s.getName().get())
                .collect(Collectors.toSet());

        String name = CommonUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

        List<TextEdit> importEdits = new ArrayList<>();
        List<TextEdit> edits = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeDescriptor, importEdits, context);
        Position pos = range.getStart();
        for (String type : types) {
            Position insertPos = new Position(pos.getLine(), pos.getCharacter());
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
        }
        return new CreateVariableOut(name, types, edits, importEdits);
    }

    static class CreateVariableOut {
        String name;
        List<String> types;
        List<TextEdit> edits;
        List<TextEdit> imports;

        public CreateVariableOut(String name, List<String> types, List<TextEdit> edits, List<TextEdit> imports) {
            this.name = name;
            this.types = types;
            this.edits = edits;
            this.imports = imports;
        }
    }
}
