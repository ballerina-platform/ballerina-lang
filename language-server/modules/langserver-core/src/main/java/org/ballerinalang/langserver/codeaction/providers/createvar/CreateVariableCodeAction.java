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
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
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
    private static final String RENAME_COMMAND = "Rename Variable";

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
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.NONE
                || isCompilationErrorTyped(typeSymbol.get())) {
            return Collections.emptyList();
        }

        String uri = context.fileUri();
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, typeSymbol.get(),
                context, new ImportsAcceptor(context));
        List<String> types = createVarTextEdits.types;
        List<CodeAction> actions = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            List<TextEdit> edits = new ArrayList<>();
            TextEdit variableEdit = createVarTextEdits.edits.get(i);
            edits.add(variableEdit);
            edits.addAll(createVarTextEdits.imports);
            String type = types.get(i);
            if (createVarTextEdits.types.size() > 1) {
                // When there's multiple types; suffix code actions with `with <type>`
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }

            CodeAction codeAction = CodeActionUtil.createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix);
            addRenamePopup(context, edits, variableEdit, codeAction, createVarTextEdits.renamePositions.get(i),
                    createVarTextEdits.varRenamePosition.get(i), createVarTextEdits.imports.size());
            actions.add(codeAction);
        }
        return actions;
    }

    private boolean isCompilationErrorTyped(TypeSymbol typeSymbol) {

        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            return isUnionCompErrorTyped((UnionTypeSymbol) typeSymbol);
        }

        return typeSymbol.typeKind() == TypeDescKind.COMPILATION_ERROR;
    }

    protected boolean isUnionCompErrorTyped(UnionTypeSymbol unionTypeSymbol) {

        return unionTypeSymbol.memberTypeDescriptors().stream()
                .anyMatch(tSymbol -> tSymbol.typeKind() == TypeDescKind.COMPILATION_ERROR);
    }

    @Override
    public String getName() {
        return NAME;
    }

    protected CreateVariableOut getCreateVariableTextEdits(Range range, DiagBasedPositionDetails positionDetails,
                                                           TypeSymbol typeDescriptor, CodeActionContext context,
                                                           ImportsAcceptor importsAcceptor) {
        Symbol matchedSymbol = positionDetails.matchedSymbol();

        Position position = PositionUtil.toPosition(positionDetails.matchedNode().lineRange().startLine());
        Set<String> allNameEntries = context.visibleSymbols(position).stream()
                .filter(s -> s.getName().isPresent())
                .map(s -> s.getName().get())
                .collect(Collectors.toSet());

        String name = NameUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

        List<TextEdit> edits = new ArrayList<>();
        List<Integer> renamePositions = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeDescriptor, context, importsAcceptor);
        Position insertPos = range.getStart();
        List<Position> varRenamePositions = new ArrayList<>();
        for (String type : types) {
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
            renamePositions.add(type.length() + 1);
            varRenamePositions.add(new Position(insertPos.getLine(),
                    insertPos.getCharacter() + type.length() + 1));
        }
        return new CreateVariableOut(name, types, edits, importsAcceptor.getNewImportTextEdits(), renamePositions,
                varRenamePositions);
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
        List<Integer> renamePositions;
        List<Position> varRenamePosition;

        public CreateVariableOut(String name, List<String> types, List<TextEdit> edits, List<TextEdit> imports,
                                 List<Integer> renamePositions, List<Position> varRenamePosition) {
            this.name = name;
            this.types = types;
            this.edits = edits;
            this.imports = imports;
            this.renamePositions = renamePositions;
            this.varRenamePosition = varRenamePosition;
        }
    }

    public void addRenamePopup(CodeActionContext context, List<TextEdit> textEdits, TextEdit variableEdit,
                               CodeAction codeAction, int renameOffset,
                               Position varRenamePosition, int newImportsCount) {
        LSClientCapabilities lsClientCapabilities = context.languageServercontext().get(LSClientCapabilities.class);
        if (lsClientCapabilities.getInitializationOptions().isPositionalRefactorRenameSupported()) {
            codeAction.setCommand(new Command(
                    CommandConstants.RENAME_COMMAND_TITLE_FOR_VARIABLE, CommandConstants.POSITIONAL_RENAME_COMMAND,
                    List.of(context.fileUri(),
                            new Position(varRenamePosition.getLine() + newImportsCount,
                                    varRenamePosition.getCharacter()))));
            return;
        }

        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (!lsClientCapabilities.getInitializationOptions().isRefactorRenameSupported() || syntaxTree.isEmpty()) {
            return;
        }
        /*
        Ex: class Test {
                function testFunc() returns error? {
                    int testResult = check test();
                }
            }
        1. startPos gives the start position of the variable type "int".
        2. If any text edits are applied before the variable creation edit, the length of those edits is added to the
           "sum". In the above example, the length of "error?" will be added.
        3. renameOffset gives the length of the variable type and the white space between the variable type and the
           variable. In the example, the renameOffset will be the length of "int ".
        */

        int startPos = CommonUtil.getTextEdit(syntaxTree.get(), variableEdit).range().startOffset();
        int sum = 0;
        for (TextEdit textEdit : textEdits) {
            io.ballerina.tools.text.TextEdit edits = CommonUtil.getTextEdit(syntaxTree.get(), textEdit);
            int startOffset = edits.range().startOffset();
            int endOffset = edits.range().endOffset();
            if (startOffset < startPos) {
                sum = sum + edits.text().length();
                if (startOffset < endOffset) {
                    int returnTypeLength = endOffset - startOffset;
                    sum = sum - returnTypeLength;
                }
            }
        }

        startPos = startPos + sum + renameOffset;

        codeAction.setCommand(
                new Command(CommandConstants.RENAME_COMMAND_TITLE_FOR_VARIABLE, CommandConstants.RENAME_COMMAND,
                        List.of(context.fileUri(), startPos)));
    }
}
