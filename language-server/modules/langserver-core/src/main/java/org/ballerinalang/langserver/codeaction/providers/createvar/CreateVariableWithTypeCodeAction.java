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
import io.ballerina.compiler.api.SemanticModel;
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
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
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
public class CreateVariableWithTypeCodeAction extends CreateVariableCodeAction {

    private static final String NAME = "Create variable with type";

    private static final String DIAGNOSTIC_CODE = "BCE4038";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return diagnostic.diagnosticInfo().code().equals(DIAGNOSTIC_CODE)
                && context.currentSemanticModel().isPresent()
                && isInRemoteMethodCallOrResourceAccess(context)
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        Optional<NonTerminalNode> actionNode = getActionNode(context);
        if (actionNode.isEmpty() || !isInActionStatement(actionNode.get())) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> typeSymbol = getReturnTypeOfAction(actionNode.get(), context);

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.ANY) {
            return Collections.emptyList();
        }

        LineRange actionLineRange = actionNode.get().lineRange();
        if (actionNode.get().parent().kind() == SyntaxKind.CHECK_EXPRESSION
                || actionNode.get().parent().kind() == SyntaxKind.CHECK_ACTION) {
            actionLineRange = actionNode.get().parent().lineRange();
        }

        String uri = context.fileUri();
        Range actionRange = PositionUtil.toRange(actionLineRange);
        CreateVariableCodeAction.CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(actionRange,
                positionDetails, typeSymbol.get(),
                context, new ImportsAcceptor(context));
        List<String> types = createVarTextEdits.types;
        List<CodeAction> codeActions = new ArrayList<>();
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

            codeActions.add(CodeActionUtil.createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix));
        }
        return codeActions;
    }

    @Override
    protected CreateVariableCodeAction.CreateVariableOut getCreateVariableTextEdits(Range range,
                                                                                    DiagBasedPositionDetails posDetails,
                                                                                    TypeSymbol typeDescriptor,
                                                                                    CodeActionContext context,
                                                                                    ImportsAcceptor importsAcceptor) {
        Symbol matchedSymbol = posDetails.matchedSymbol();

        Position position = PositionUtil.toPosition(posDetails.matchedNode().lineRange().startLine());
        Set<String> allNameEntries = context.visibleSymbols(position).stream()
                .filter(s -> s.getName().isPresent())
                .map(s -> s.getName().get())
                .collect(Collectors.toSet());

        String name = NameUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

        List<TextEdit> edits = new ArrayList<>();
        List<Integer> renamePositions = new ArrayList<>();
        List<String> types = getPossibleTypes(typeDescriptor, context);
        Position insertPos = range.getStart();
        List<Position> varRenamePositions = new ArrayList<>();
        for (String type : types) {
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
            renamePositions.add(type.length() + 1);
            varRenamePositions.add(new Position(insertPos.getLine(),
                    insertPos.getCharacter() + type.length() + 1));
        }
        return new CreateVariableCodeAction.CreateVariableOut(name, types, edits,
                importsAcceptor.getNewImportTextEdits(), renamePositions, varRenamePositions);
    }

    /**
     * Get all possible return type combinations for a method with an inferred return type.
     * Here if the type symbol is a union we will check whether it contains an error member.
     * Other members of the union will be combined with the error type and returned.
     * If union has `any` type member, `any` type member will be discarded when combining with the error type.
     *
     * @param typeSymbol Return type descriptor of the method.
     * @param context    CodeActionContext
     * @return {@link List<String>}
     */
    private List<String> getPossibleTypes(TypeSymbol typeSymbol, CodeActionContext context) {
        typeSymbol = getRawType(typeSymbol, context);
        Set<String> possibleTypes = new HashSet<>();
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
                        } else if (memberTypeSymbol.typeKind() == TypeDescKind.ERROR ||
                                CommonUtil.getRawType(memberTypeSymbol).typeKind() == TypeDescKind.ERROR) {
                            errorTypes.add(memberTypeSymbol);
                        } else {
                            possibleTypes.add(getTypeName(memberTypeSymbol, context));
                        }
                    });
        } else {
            String type = getTypeName(typeSymbol, context);
            if (!"any".equals(type)) {
                return Collections.singletonList(type);
            }
        }

        if (!errorTypes.isEmpty()) {
            String errorTypeStr = errorTypes.stream()
                    .map(type -> getTypeName(type, context))
                    .collect(Collectors.joining("|"));
            return possibleTypes.stream()
                    .filter(type -> !"any".equals(type))
                    .map(type -> type + "|" + errorTypeStr)
                    .collect(Collectors.toList());
        }
        return possibleTypes.stream()
                .filter(type -> !"any".equals(type))
                .collect(Collectors.toList());
    }

    private boolean isInRemoteMethodCallOrResourceAccess(CodeActionContext context) {
        Node evalNode = context.nodeAtRange();
        int count = 0;
        while (evalNode.kind() != SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION
                && evalNode.kind() != SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
            count++;
            if (count == 3) {
                return false;
            }
            evalNode = evalNode.parent();
        }
        return true;
    }

    /**
     * Check if action node is inside an action statement node.
     *
     * @param actionNode Action node
     * @return True if the action node is within an action statement
     */
    private boolean isInActionStatement(NonTerminalNode actionNode) {
        if (actionNode.parent().kind() == SyntaxKind.CHECK_ACTION
                || actionNode.parent().kind() == SyntaxKind.CHECK_EXPRESSION) {
            actionNode = actionNode.parent();
        }

        return actionNode.parent().kind() == SyntaxKind.ACTION_STATEMENT;
    }

    private String getTypeName(TypeSymbol symbol, CodeActionContext context) {
        Optional<ModuleSymbol> module = symbol.getModule();
        if (module.isPresent()) {
            String fqPrefix = "";
            ModuleID id = module.get().id();
            if (!ProjectConstants.ANON_ORG.equals(id.orgName()) && !isLangAnnotationModule(id)) {
                fqPrefix = id.orgName() + "/" + id.moduleName() + ":" + id.version() + ":";
            }
            String moduleQualifiedName = fqPrefix + (symbol.getName().isPresent() ? symbol.getName().get()
                    : getRawType(symbol, context).signature());
            return FunctionGenerator.processModuleIDsInText(new ImportsAcceptor(context), moduleQualifiedName, context);
        }
        return symbol.signature();
    }

    private boolean isLangAnnotationModule(ModuleID moduleID) {
        return moduleID.orgName().equals("ballerina") && moduleID.moduleName().equals("lang.annotations");
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

    /**
     * Finds and returns the action node in focus.
     *
     * @param context Code action context
     * @return Action node (optional)
     */
    private Optional<NonTerminalNode> getActionNode(CodeActionContext context) {
        ActionNodeFinder actionNodeFinder = new ActionNodeFinder();
        return actionNodeFinder.findActionNode(context.nodeAtRange());
    }

    /**
     * Finds the type symbol of the return type(s) of the action node in focus.
     *
     * @param actionNode Action node
     * @param context    Code action context
     * @return Type(s) expected to be returned from the action invocation
     */
    private Optional<TypeSymbol> getReturnTypeOfAction(NonTerminalNode actionNode,
                                                       CodeActionContext context) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return Optional.empty();
        }
        Optional<Symbol> symbol = semanticModel.flatMap(model -> model.symbol(actionNode));
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        Optional<TypeSymbol> returnTypeDesc = Optional.empty();
        if (symbol.get().kind() == SymbolKind.METHOD) {
            returnTypeDesc = ((MethodSymbol) symbol.get()).typeDescriptor().returnTypeDescriptor();
        } else if (symbol.get().kind() == SymbolKind.RESOURCE_METHOD) {
            returnTypeDesc = ((ResourceMethodSymbol) symbol.get()).typeDescriptor().returnTypeDescriptor();
        }

        if (returnTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        NonTerminalNode parent = actionNode.parent();
        // If action node is not within a check expr/action or not returning a union, we can just return
        if ((parent.kind() != SyntaxKind.CHECK_EXPRESSION && parent.kind() != SyntaxKind.CHECK_ACTION)
                || returnTypeDesc.get().typeKind() != TypeDescKind.UNION) {
            return returnTypeDesc;
        }

        // By this point, 
        // 1. We are within a check expression/action
        // 2. Return type is a union

        List<TypeSymbol> memberTypes = ((UnionTypeSymbol) returnTypeDesc.get()).memberTypeDescriptors().stream()
                .filter(member -> CommonUtil.getRawType(member).typeKind() != TypeDescKind.ERROR)
                .collect(Collectors.toList());

        if (memberTypes.isEmpty()) {
            // If there are no non-error members (which is highly unlikely), there's no return type
            return Optional.empty();
        } else if (memberTypes.size() == 1) {
            // If there's only 1 non-error member type, we can just return that
            return Optional.of(memberTypes.get(0));
        }

        // Otherwise, create a new union type with the non-error members
        UnionTypeSymbol unionTypeSymbol = semanticModel.get().types().builder().UNION_TYPE
                .withMemberTypes(memberTypes.toArray(new TypeSymbol[0]))
                .build();
        return Optional.of(unionTypeSymbol);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * A visitor to find RemoteMethodCallNodes and ClientResourceAccessActionNodes.
     */
    static class ActionNodeFinder extends NodeVisitor {

        private NonTerminalNode actionNode = null;

        /**
         * Use this method to invoke this visitor in order to avoid getting an NPE when the provided node is of
         * SyntaxKind LIST.
         *
         * @param node Node at cursor
         * @return Optional action node valid for this code action
         */
        public Optional<NonTerminalNode> findActionNode(Node node) {
            if (node.kind() == SyntaxKind.LIST) {
                node.parent().accept(this);
            }
            node.accept(this);
            return Optional.ofNullable(actionNode);
        }

        @Override
        public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
            simpleNameReferenceNode.parent().accept(this);
        }

        @Override
        public void visit(NamedArgumentNode namedArgumentNode) {
            namedArgumentNode.parent().accept(this);
        }

        @Override
        public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
            this.actionNode = remoteMethodCallActionNode;
        }

        @Override
        public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {
            this.actionNode = clientResourceAccessActionNode;
        }

        @Override
        protected void visitSyntaxNode(Node node) {
            NonTerminalNode parent = node.parent();
            if (parent == null) {
                return;
            }
            
            node.parent().accept(this);
        }
    }
}
