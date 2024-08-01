/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
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
 * Code Action for extracting to a configurable variable.
 *
 * @since 2201.10.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToConfigurableCodeAction implements RangeBasedCodeActionProvider {

    private static final String CONFIGURABLE_NAME_PREFIX = "config";
    private static final List<SyntaxKind> argumentSyntaxList = List.of(SyntaxKind.FUNCTION_CALL,
            SyntaxKind.METHOD_CALL, SyntaxKind.EXPLICIT_NEW_EXPRESSION, SyntaxKind.IMPLICIT_NEW_EXPRESSION);
    private static final List<SyntaxKind> supportedExpressionList = List.of(SyntaxKind.BOOLEAN_LITERAL,
            SyntaxKind.NUMERIC_LITERAL, SyntaxKind.STRING_LITERAL, SyntaxKind.UNARY_EXPRESSION,
            SyntaxKind.XML_TEMPLATE_EXPRESSION, SyntaxKind.STRING_TEMPLATE_EXPRESSION,
            SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.LIST_CONSTRUCTOR, SyntaxKind.TABLE_CONSTRUCTOR);

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL, SyntaxKind.STRING_LITERAL,
                SyntaxKind.UNARY_EXPRESSION, SyntaxKind.XML_TEMPLATE_EXPRESSION, SyntaxKind.STRING_TEMPLATE_EXPRESSION,
                SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.LIST_CONSTRUCTOR, SyntaxKind.TABLE_CONSTRUCTOR,
                SyntaxKind.FUNCTION_CALL, SyntaxKind.METHOD_CALL,
                SyntaxKind.EXPLICIT_NEW_EXPRESSION, SyntaxKind.IMPLICIT_NEW_EXPRESSION);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Node node = positionDetails.matchedCodeActionNode();
        if (hasMultipleArgsSelected(node)) {
            return context.nodeAtRange().kind() == SyntaxKind.LIST;
        }
        SyntaxKind parentKind = node.parent().kind();
        return context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent()
                && parentKind != SyntaxKind.CONST_DECLARATION
                && parentKind != SyntaxKind.MODULE_VAR_DECL
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        SemanticModel semanticModel = context.currentSemanticModel().get();
        Node node = posDetails.matchedCodeActionNode();
        if (hasMultipleArgsSelected(node)) {
            FunctionTypeSymbol functionTypeSymbol;
            SeparatedNodeList<?> separatedNodeList;
            switch (node.kind()) {
                case FUNCTION_CALL -> {
                    Optional<Symbol> functionSymbol = semanticModel.symbol(node);
                    if (functionSymbol.isEmpty()) {
                        return Collections.emptyList();
                    }
                    functionTypeSymbol = ((FunctionSymbol) functionSymbol.get()).typeDescriptor();
                    separatedNodeList = ((FunctionCallExpressionNode) node).arguments();
                }
                case METHOD_CALL -> {
                    Optional<Symbol> methodCall = semanticModel.symbol(node);
                    if (methodCall.isEmpty()) {
                        return Collections.emptyList();
                    }
                    functionTypeSymbol = ((MethodSymbol) methodCall.get()).typeDescriptor();
                    separatedNodeList = ((MethodCallExpressionNode) node).arguments();
                }
                case IMPLICIT_NEW_EXPRESSION -> {
                    Optional<ParenthesizedArgList> parenthesizedArgList =
                            ((ImplicitNewExpressionNode) node).parenthesizedArgList();
                    if (parenthesizedArgList.isEmpty()) {
                        return Collections.emptyList();
                    }
                    Optional<FunctionTypeSymbol> initType = getFunctionTypeSymbolFromNewExpr(semanticModel, node);
                    if (initType.isEmpty()) {
                        return Collections.emptyList();
                    }
                    functionTypeSymbol = initType.get();
                    separatedNodeList = parenthesizedArgList.get().arguments();
                }
                case EXPLICIT_NEW_EXPRESSION -> {
                    Optional<FunctionTypeSymbol> initType = getFunctionTypeSymbolFromNewExpr(semanticModel, node);
                    if (initType.isEmpty()) {
                        return Collections.emptyList();
                    }
                    functionTypeSymbol = initType.get();
                    separatedNodeList = ((ExplicitNewExpressionNode) node).parenthesizedArgList().arguments();
                }
                default -> {
                    return Collections.emptyList();
                }
            }

            if (functionTypeSymbol.params().isEmpty()) {
                return Collections.emptyList();
            }
            List<ParameterSymbol> parameterSymbols = functionTypeSymbol.params().get();
            TypeSymbol anydataType = semanticModel.types().ANYDATA;
            List<TextEdit> textEdits = new ArrayList<>();
            boolean firstEdit = true;
            for (int argIdx = 0; argIdx < separatedNodeList.size() && argIdx < parameterSymbols.size(); argIdx++) {
                Node argument = separatedNodeList.get(argIdx);
                if (argument == null) {
                    return Collections.emptyList();
                }
                if (withInRange(argument, context.range())) {
                    SyntaxKind argKind = argument.kind();
                    if (argKind == SyntaxKind.NAMED_ARG) {
                        argument = ((NamedArgumentNode) argument).expression();
                    } else if (argKind == SyntaxKind.POSITIONAL_ARG) {
                        argument = ((PositionalArgumentNode) argument).expression();
                    } else {
                        return Collections.emptyList();
                    }

                    if (!supportedExpressionList.contains(argument.kind())) {
                        return Collections.emptyList();
                    }
                    Optional<TypeSymbol> argTypeSymbol = semanticModel.typeOf(argument);
                    if (argTypeSymbol.isEmpty()) {
                        return Collections.emptyList();
                    }
                    TypeSymbol argType = argTypeSymbol.get();
                    if (!argType.subtypeOf(anydataType)) {
                        return Collections.emptyList();
                    }
                    ParameterSymbol parameterSymbol = parameterSymbols.get(argIdx);
                    String paramName = parameterSymbol.getName().orElse(CONFIGURABLE_NAME_PREFIX);
                    String confName = getConfigurableName(context, paramName);
                    ConfigurableData configurableData = getConfigurableData(context);
                    Position positionToAddConfVar = configurableData.position();
                    boolean addNewLineAtStart = configurableData.addNewLineAtStart() && firstEdit;
                    firstEdit = false;

                   textEdits.addAll(getTextEdits(context, argument, argType, confName, positionToAddConfVar,
                           addNewLineAtStart));
                }
            }
            CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONFIGURABLE,
                    textEdits, context.fileUri(), CodeActionKind.RefactorExtract);
            return List.of(codeAction);
        }

        Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(node);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        String confName = getConfigurableName(context);
        ConfigurableData configurableData = getConfigurableData(context);
        Position positionToAddConfVar = configurableData.position();
        boolean addNewLineAtStart = configurableData.addNewLineAtStart();

        List<TextEdit> textEdits = getTextEdits(context, node, typeSymbol.get(), confName, positionToAddConfVar,
                addNewLineAtStart);

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONFIGURABLE,
                textEdits, context.fileUri(), CodeActionKind.RefactorExtract);
        CodeActionUtil.addRenamePopup(context, codeAction, CommandConstants.RENAME_COMMAND_TITLE_FOR_CONFIGURABLE,
                getRenamePosition(textEdits.get(1).getRange(), addNewLineAtStart));
        return Collections.singletonList(codeAction);
    }

    private static Optional<FunctionTypeSymbol> getFunctionTypeSymbolFromNewExpr(SemanticModel semanticModel,
                                                                                 Node node) {
        Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(node);
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol.get());
        Optional<ClassSymbol> classSymbol;
        if (rawType.typeKind() == TypeDescKind.UNION) {
            classSymbol = ((UnionTypeSymbol) rawType).memberTypeDescriptors()
                    .stream()
                    .map(CommonUtil::getRawType)
                    .filter(symbol -> symbol.kind() == SymbolKind.CLASS)
                    .findFirst()
                    .map(sym -> (ClassSymbol) sym);
        } else {
            classSymbol = Optional.of((ClassSymbol) rawType);
        }
        if (classSymbol.isEmpty()) {
            return Optional.empty();
        }
        return classSymbol.flatMap(ClassSymbol::initMethod)
                .map(FunctionSymbol::typeDescriptor);
    }

    private Position getRenamePosition(Range range, boolean addNewLineAtStart) {
        // line position will increment by one due to const declaration statement
        int line = range.getEnd().getLine() + 1;
        if (addNewLineAtStart) {
            line += 1;
        }
        return new Position(line, range.getStart().getCharacter());
    }

    @Override
    public String getName() {
        return CommandConstants.EXTRACT_TO_CONFIGURABLE;
    }

    private static List<TextEdit> getTextEdits(CodeActionContext context, Node node, TypeSymbol typeSymbol,
                                               String confName, Position positionToAddConfVar,
                                               boolean addNewLineAtStart) {
        String typeDescriptor = FunctionGenerator.getReturnTypeAsString(context, typeSymbol.signature());
        String value = node.toSourceCode().strip();
        LineRange replaceRange = node.lineRange();
        String confDeclaration = addNewLineAtStart ? System.lineSeparator() : "";
        confDeclaration += String.format("configurable %s %s = %s;%n", typeDescriptor, confName, value);

        TextEdit confDecEdit = new TextEdit(new Range(positionToAddConfVar, positionToAddConfVar), confDeclaration);
        TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                PositionUtil.toPosition(replaceRange.endLine())), confName);
        return List.of(confDecEdit, replaceEdit);
    }

    private static ConfigurableData getConfigurableData(CodeActionContext context) {
        ModulePartNode modulePartNode = context.currentSyntaxTree().get().rootNode();
        NodeList<ImportDeclarationNode> importsList = modulePartNode.imports();

        ModuleVariableDeclarationNode configurableVarNode = null;
        for (Node node: modulePartNode.children()) {
            if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
                ModuleVariableDeclarationNode modVarDeclarationNode = (ModuleVariableDeclarationNode) node;
                if (hasConfigurableQualifier(modVarDeclarationNode)) {
                    configurableVarNode = modVarDeclarationNode;
                    break;
                }
            }
        }

        if (configurableVarNode != null) {
            return new ConfigurableData(new Position(configurableVarNode.lineRange().startLine().line(), 0),
                    !configurableVarNode.toString().startsWith(System.lineSeparator()));
        } else if (importsList.isEmpty()) {
            return new ConfigurableData(
                    PositionUtil.toPosition(modulePartNode.lineRange().startLine()), false);
        }
        ImportDeclarationNode lastImport = importsList.get(importsList.size() - 1);
        return new ConfigurableData(new Position(lastImport.lineRange().endLine().line() + 1, 0), true);
    }

    private static String getConfigurableName(CodeActionContext context) {
        Position pos = context.range().getEnd();
        Set<String> allNames = context.visibleSymbols(new Position(pos.getLine(), pos.getCharacter())).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return NameUtil.generateTypeName(CONFIGURABLE_NAME_PREFIX, allNames);
    }

    private static String getConfigurableName(CodeActionContext context, String paramName) {
        Position pos = context.range().getEnd();
        Set<String> allNames = context.visibleSymbols(new Position(pos.getLine(), pos.getCharacter())).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return NameUtil.generateTypeName(paramName, allNames);
    }

    private static boolean hasConfigurableQualifier(ModuleVariableDeclarationNode modVarDeclarationNode) {
        return modVarDeclarationNode.qualifiers()
                .stream().anyMatch(q -> q.text().equals(Qualifier.CONFIGURABLE.getValue()));
    }

    private static boolean hasMultipleArgsSelected(Node node) {
        return argumentSyntaxList.contains(node.kind());
    }

    private static boolean withInRange(Node argument, Range range) {
        return PositionUtil.isWithinRange(PositionUtil.toPosition(argument.lineRange().startLine()), range) ||
                PositionUtil.isWithinRange(PositionUtil.toPosition(argument.lineRange().endLine()), range);
    }

    private record ConfigurableData(Position position, boolean addNewLineAtStart) {
    }
}
