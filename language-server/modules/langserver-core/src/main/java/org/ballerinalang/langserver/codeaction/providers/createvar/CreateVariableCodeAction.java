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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.NameUtil;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Create Variable";

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 999;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<TypeSymbol> typeSymbol = getExpectedTypeSymbol(positionDetails);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.NONE) {
            return Collections.emptyList();
        }

        String uri = context.fileUri();
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, typeSymbol.get(),
                context);
        List<String> types = createVarTextEdits.types;
        List<CodeAction> actions = new ArrayList<>();
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
            actions.add(CodeActionUtil.createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix));
        }
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    protected CreateVariableOut getCreateVariableTextEdits(Range range, DiagBasedPositionDetails positionDetails,
                                                           TypeSymbol typeDescriptor, CodeActionContext context) {
        Symbol matchedSymbol = positionDetails.matchedSymbol();

        Position position = PositionUtil.toPosition(positionDetails.matchedNode().lineRange().startLine());
        Set<String> allNameEntries = context.visibleSymbols(position).stream()
                .filter(s -> s.getName().isPresent())
                .map(s -> s.getName().get())
                .collect(Collectors.toSet());

        String name = NameUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

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

    /**
     * Given the position details, this method will determine the expected type symbol for the required variable
     * assignment from diagnostic properties.
     *
     * @param positionDetails Position details
     * @return Optional expected type symbol
     */
    protected Optional<TypeSymbol> getExpectedTypeSymbol(DiagBasedPositionDetails positionDetails) {
        Optional<Symbol> symbol = positionDetails.diagnosticProperty(
                CodeActionUtil.getDiagPropertyFilterFunction(
                        DiagBasedPositionDetails.DIAG_PROP_VAR_ASSIGN_SYMBOL_INDEX));
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol typeSymbol = null;
        if (symbol.get() instanceof TypeSymbol) {
            typeSymbol = (TypeSymbol) symbol.get();
        }

        if (symbol.get().kind() == SymbolKind.WORKER) {
            WorkerSymbol workerSymbol = (WorkerSymbol) symbol.get();
            typeSymbol = workerSymbol.returnType();
        }

        return Optional.ofNullable(typeSymbol);
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
