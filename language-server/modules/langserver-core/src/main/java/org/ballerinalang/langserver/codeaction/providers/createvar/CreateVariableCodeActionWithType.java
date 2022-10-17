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
package org.ballerinalang.langserver.codeaction.providers.createvar;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create variable code action when type infer diagnostic is presented.
 *
 * @since 2201.4.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeActionWithType implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Create variable with type";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, CodeActionContext context) {
        return diagnostic.diagnosticInfo().code().equals("BCE3934")
                && context.nodeAtRange().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION
                || context.nodeAtRange().kind() == SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<TypeSymbol> typeSymbol = getReturnTypeDescriptorOfMethod(context);

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.ANY) {
            return Collections.emptyList();
        }

        String uri = context.fileUri();
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableCodeAction.CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range,
                positionDetails, typeSymbol.get(),
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

            actions.add(CodeActionUtil.createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix));
        }
        return actions;
    }

    private CreateVariableCodeAction.CreateVariableOut getCreateVariableTextEdits(Range range,
                                                                                  DiagBasedPositionDetails positionDetails,
                                                                                  TypeSymbol typeDescriptor,
                                                                                  CodeActionContext context,
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
        List<String> types = getPossibleTypes(typeDescriptor, context);
        Position pos = range.getStart();
        for (String type : types) {
            Position insertPos = new Position(pos.getLine(), pos.getCharacter());
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
            renamePositions.add(type.length() + 1);
        }
        return new CreateVariableCodeAction.CreateVariableOut(name, types, edits, importsAcceptor.getNewImportTextEdits(), renamePositions);
    }

    /**
     * Get all possible return type combinations for the type infer required method.
     * Here if the type symbol is a union we will check it contains an error member.
     * Other members of the union will then combine with the error type and produce the output.
     * In here we will `any` type.
     *
     * @param typeSymbol Return type descriptor of the method.
     * @param context    CodeActionContext
     * @return {@link List<String>}
     */
    private List<String> getPossibleTypes(TypeSymbol typeSymbol, CodeActionContext context) {
        typeSymbol = getRawType(typeSymbol, context);
        Set<String> possibleTypes = new HashSet<>();
        List<String> finalPossibleTypeSet = new ArrayList<>();
        List<TypeSymbol> errorTypes = new ArrayList<>();
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            ((UnionTypeSymbol) typeSymbol)
                    .memberTypeDescriptors()
                    .stream()
                    .map(memberTypeSymbol -> getRawType(memberTypeSymbol, context))
                    .forEach(memberTypeSymbol -> {
                        if (memberTypeSymbol.typeKind() == TypeDescKind.UNION) {
                            possibleTypes.addAll(
                                    ((UnionTypeSymbol) memberTypeSymbol)
                                            .memberTypeDescriptors()
                                            .stream()
                                            .map(memberTSymbol -> getRawType(memberTSymbol, context))
                                            .map(symbol -> getTypeName(symbol, context))
                                            .collect(Collectors.toList()));
                        } else {
                            if (memberTypeSymbol.typeKind() == TypeDescKind.ERROR
                                    || CommonUtil.getRawType(memberTypeSymbol).typeKind() == TypeDescKind.ERROR) {
                                errorTypes.add(memberTypeSymbol);
                            } else {
                                possibleTypes.add(getTypeName(memberTypeSymbol, context));
                            }
                        }
                    });
        } else {
            String type = getTypeName(typeSymbol, context);
            if (!type.equals("any")) {
                return Collections.singletonList(type);
            }
        }

        if (!errorTypes.isEmpty()) {
            String errorTypeStr = errorTypes.stream()
                    .map(type -> getTypeName(type, context))
                    .collect(Collectors.joining("|"));
            finalPossibleTypeSet.addAll(possibleTypes.stream()
                    .filter(type -> !type.equals("any"))
                    .map(type -> type + "|" + errorTypeStr).collect(Collectors.toSet()));
        } else {
            finalPossibleTypeSet.addAll(possibleTypes.stream()
                    .filter(type -> !type.equals("any"))
                    .collect(Collectors.toSet()));
        }
        return finalPossibleTypeSet;
    }

    private String getTypeName(TypeSymbol symbol, CodeActionContext context) {
        Optional<ModuleSymbol> module = symbol.getModule();
        if (module.isPresent()) {
            String fqPrefix = "";
            if (!(ProjectConstants.ANON_ORG.equals(module.get().id().orgName()))) {
                ModuleID id = module.get().id();
                fqPrefix = id.orgName() + "/" + id.moduleName() + ":" + id.version() + ":";
            }
            String moduleQualifiedName = fqPrefix + (symbol.getName().isPresent() ? symbol.getName().get() : "");
            return FunctionGenerator.processModuleIDsInText(new ImportsAcceptor(context), moduleQualifiedName, context);
        }
        return symbol.signature();
    }

    private TypeSymbol getRawType(TypeSymbol typeSymbol, CodeActionContext context) {
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        Types types = context.currentSemanticModel().get().types();
        TypeBuilder builder = types.builder();
        RecordTypeSymbol recordTypeSymbol = builder.RECORD_TYPE.withRestField(types.ANY).build();
        if (rawType.subtypeOf(types.ERROR) || rawType.subtypeOf(recordTypeSymbol)) {
            return typeSymbol;
        }
        return rawType;
    }

    private Optional<TypeSymbol> getReturnTypeDescriptorOfMethod(CodeActionContext context) {
        return context.currentSemanticModel()
                .flatMap(model -> model.symbol(context.nodeAtRange()))
                .filter(symbol -> symbol.kind() == SymbolKind.METHOD || symbol.kind() == SymbolKind.RESOURCE_METHOD)
                .flatMap(symbol ->
                        symbol.kind() == SymbolKind.METHOD ?
                                ((MethodSymbol) symbol).typeDescriptor().returnTypeDescriptor() :
                                ((ResourceMethodSymbol) symbol).typeDescriptor().returnTypeDescriptor());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
