/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.RecordUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for extracting a transform function from a mapping.
 *
 * @since 2201.9.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToTransformFunctionCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "extract to transform function";
    private static final String EXTRACTED_PREFIX = "transform";

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        ExpressionNode valueExprNode;
        SpecificFieldNode specificFieldNode;
        RecordTypeWrapper inputRecordTypeWrapper, outputRecordTypeWrapper;
        SemanticModel semanticModel;
        Node enclosingNode;
        Document currentDocument;

        // Extracting the input and output record types
        try {
            NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
            if (matchedCodeActionNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
                specificFieldNode = (SpecificFieldNode) matchedCodeActionNode;
                valueExprNode = specificFieldNode.valueExpr().orElseThrow();
            } else {
                specificFieldNode = (SpecificFieldNode) matchedCodeActionNode.parent();
                valueExprNode = (ExpressionNode) matchedCodeActionNode;
            }

            semanticModel = context.currentSemanticModel().orElseThrow();
            TypeSymbol matchedTypeSymbol = posDetails.matchedTopLevelTypeSymbol();
            Symbol inputRecordSymbol = matchedTypeSymbol == null ?
                    semanticModel.symbol(context.nodeAtRange()).orElseThrow() : matchedTypeSymbol;
            inputRecordTypeWrapper = getReferredRecordSymbol(context,
                    getTypeSymbol(inputRecordSymbol).orElseThrow()).orElseThrow();
            Node fieldNameNode = (specificFieldNode).fieldName();

            Symbol outputRecordSymbol = semanticModel.symbol(fieldNameNode).orElseThrow();
            outputRecordTypeWrapper = getReferredRecordSymbol(context,
                    getTypeSymbol(outputRecordSymbol).orElseThrow()).orElseThrow();

            enclosingNode = CommonUtil.getMatchingNode(specificFieldNode,
                    node -> node.parent().kind() == SyntaxKind.MODULE_PART).orElseThrow();
            currentDocument = context.currentDocument().orElseThrow();
        } catch (Exception e) {
            return Collections.emptyList();
        }

        // Extracting the line position and the range
        LinePosition functionEndLine = enclosingNode.lineRange().endLine();
        Range extractedRange = PositionUtil.toRange(valueExprNode.lineRange());

        // Generating the transformation function and the function call
        List<Symbol> visibleSymbols = semanticModel.visibleSymbols(currentDocument, functionEndLine);
        String functionName = FunctionGenerator.generateFunctionName(EXTRACTED_PREFIX, visibleSymbols);
        String extractedFunction = getFunction(context, functionName, outputRecordTypeWrapper,
                inputRecordTypeWrapper, valueExprNode, visibleSymbols);
        String functionCall = functionName + CommonKeys.OPEN_PARENTHESES_KEY +
                valueExprNode.toSourceCode().stripTrailing() +
                CommonKeys.CLOSE_PARENTHESES_KEY;

        // Generating the text edits and the code action
        List<TextEdit> textEdits = List.of(
                new TextEdit(PositionUtil.toRange(functionEndLine), extractedFunction),
                new TextEdit(extractedRange, functionCall)
        );
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_TRANSFORM_FUNCTION,
                textEdits, context.fileUri(), CodeActionKind.RefactorExtract);
        CodeActionUtil.addRenamePopup(context, codeAction, CommandConstants.RENAME_COMMAND_TITLE_FOR_FUNCTION,
                extractedRange.getStart());
        return Collections.singletonList(codeAction);
    }

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(
                SyntaxKind.FIELD_ACCESS,
                SyntaxKind.OPTIONAL_FIELD_ACCESS,
                SyntaxKind.SPECIFIC_FIELD,
                SyntaxKind.SIMPLE_NAME_REFERENCE,
                SyntaxKind.QUALIFIED_NAME_REFERENCE,
                SyntaxKind.INDEXED_EXPRESSION,
                SyntaxKind.FUNCTION_CALL,
                SyntaxKind.METHOD_CALL,
                SyntaxKind.CHECK_EXPRESSION,
                SyntaxKind.LET_EXPRESSION
        );
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return (positionDetails.matchedCodeActionNode().parent().kind() == SyntaxKind.SPECIFIC_FIELD ||
                positionDetails.matchedCodeActionNode().kind() == SyntaxKind.SPECIFIC_FIELD) &&
                CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    private static Optional<RecordTypeWrapper> getReferredRecordSymbol(CodeActionContext context,
                                                                       TypeSymbol typeSymbol) {
        try {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            String name = CodeActionUtil.getPossibleType(typeReferenceTypeSymbol, context).orElseThrow();
            TypeSymbol typeDescriptorTypeSymbol = typeReferenceTypeSymbol.typeDescriptor();
            return Optional.of(new RecordTypeWrapper((RecordTypeSymbol) typeDescriptorTypeSymbol, name));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String getFunction(CodeActionContext context, String functionName,
                                      RecordTypeWrapper outputRecordTypeWrapper,
                                      RecordTypeWrapper inputRecordTypeWrapper,
                                      NonTerminalNode matchedNode, List<Symbol> visibleSymbols) {

        // Obtain the parameter name
        ParameterNameFinder parameterNameFinder = new ParameterNameFinder();
        matchedNode.accept(parameterNameFinder);
        Set<String> visibleSymbolNames = visibleSymbols.stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        String fieldName = NameUtil.generateParameterName(parameterNameFinder.getParameterName(), 0,
                inputRecordTypeWrapper.recordTypeSymbol, visibleSymbolNames);

        // Generating the transform function
        Map<String, RecordFieldSymbol> recordFieldSymbolMap =
                outputRecordTypeWrapper.recordTypeSymbol.fieldDescriptors();
        String bodyText = recordFieldSymbolMap.isEmpty() ? "" :
                RecordUtil.getFillAllRecordFieldInsertText(recordFieldSymbolMap);
        String parameterName = inputRecordTypeWrapper.recordTypeName() + " " + fieldName;
        String generatedFunction = String.format("%s %s %s%s%s returns %s %s %s%n    %s%n%s",
                CommonKeys.FUNCTION_KEYWORD_KEY, functionName, CommonKeys.OPEN_PARENTHESES_KEY, parameterName,
                CommonKeys.CLOSE_PARENTHESES_KEY, outputRecordTypeWrapper.recordTypeName(),
                CommonKeys.ARROW_FUNCTION_SYMBOL_KEY, CommonKeys.OPEN_BRACE_KEY, bodyText,
                CommonKeys.CLOSE_BRACE_KEY + CommonKeys.SEMI_COLON_SYMBOL_KEY);

        // Formatting the generated function
        try {
            generatedFunction = Formatter.format(generatedFunction);
        } catch (FormatterException e) {
            // Ignore the formatter exception
        }
        return CommonUtil.LINE_SEPARATOR + CommonUtil.LINE_SEPARATOR + generatedFunction;
    }

    private static Optional<TypeSymbol> getTypeSymbol(Symbol symbol) {
        TypeSymbol typeSymbol;
        switch (symbol.kind()) {
            case RECORD_FIELD -> typeSymbol = ((RecordFieldSymbol) symbol).typeDescriptor();
            case PARAMETER -> typeSymbol = ((ParameterSymbol) symbol).typeDescriptor();
            case VARIABLE -> typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
            case TYPE -> typeSymbol = (TypeSymbol) symbol;
            default -> {
                return Optional.empty();
            }
        }
        return Optional.of(typeSymbol);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * A record class that wraps a RecordTypeSymbol and its name as a String.
     *
     * @param recordTypeSymbol The symbol representation of the record type.
     * @param recordTypeName   The name of the record type as a String.
     */
    private record RecordTypeWrapper(RecordTypeSymbol recordTypeSymbol, String recordTypeName) {

    }

    private static class ParameterNameFinder extends NodeVisitor {

        private String parameterName;

        ParameterNameFinder() {
            this.parameterName = "var1";
        }

        public String getParameterName() {
            return parameterName;
        }

        @Override
        public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
            this.parameterName = simpleNameReferenceNode.name().text();
        }

        @Override
        public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
            fieldAccessExpressionNode.fieldName().accept(this);
        }

        @Override
        public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
            this.parameterName = "";
        }
    }
}
