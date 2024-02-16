/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
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
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.RecordUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        // Check if the referenced match symbol has a type of record
        TypeSymbol matchedTypeSymbol = posDetails.matchedTopLevelTypeSymbol();
        RecordTypeWrapper inputRecordTypeWrapper = getReferredRecordSymbol(matchedTypeSymbol);
        if (inputRecordTypeWrapper == null) {
            return Collections.emptyList();
        }

        // Check if the parent node is a specific field
        NonTerminalNode parentNode = posDetails.matchedCodeActionNode().parent();
        if (parentNode.kind() != SyntaxKind.SPECIFIC_FIELD) {
            return Collections.emptyList();
        }

        // Check if the field node has a type of record
        Node fieldNameNode = ((SpecificFieldNode) parentNode).fieldName();
        if (fieldNameNode.kind() != SyntaxKind.IDENTIFIER_TOKEN) {
            return Collections.emptyList();
        }

        IdentifierToken fieldName = (IdentifierToken) fieldNameNode;
        Optional<Symbol> optionalFieldNameSymbol =
                context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(fieldName));
        if (optionalFieldNameSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        Symbol fieldSymbol = optionalFieldNameSymbol.get();
        if (fieldSymbol.kind() != SymbolKind.RECORD_FIELD) {
            return Collections.emptyList();
        }
        RecordTypeWrapper outputRecordTypeWrapper =
                getReferredRecordSymbol(((RecordFieldSymbol) fieldSymbol).typeDescriptor());
        if (outputRecordTypeWrapper == null) {
            return Collections.emptyList();
        }
        RecordTypeSymbol outputRecordSymbol = outputRecordTypeWrapper.recordTypeSymbol();

        List<TextEdit> textEdits = new ArrayList<>();

        Map<String, RecordFieldSymbol> stringRecordFieldSymbolMap = outputRecordSymbol.fieldDescriptors();
        if (stringRecordFieldSymbolMap.isEmpty()) {
            return Collections.emptyList();
        }

        String parameterName = inputRecordTypeWrapper.recordTypeName + " " + fieldName;
        String returnSignature =
                FunctionGenerator.getReturnTypeAsString(context, outputRecordTypeWrapper.recordTypeName());

        Optional<Node> enclosingNode =
                CommonUtil.getMatchingNode(parentNode, node -> node.parent().kind() == SyntaxKind.MODULE_PART);
        if (enclosingNode.isEmpty()) {
            return Collections.emptyList();
        }

        Position position = PositionUtil.toPosition(enclosingNode.get().lineRange().endLine());
        LinePosition linePosition = PositionUtil.getLinePosition(position);

        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return Collections.emptyList();
        }
        List<Symbol> visibleSymbols = semanticModel.get().visibleSymbols(context.currentDocument().get(), linePosition);

        String functionName = FunctionGenerator.generateFunctionName(EXTRACTED_PREFIX, visibleSymbols);
        String editText = RecordUtil.getFillAllRecordFieldInsertText(stringRecordFieldSymbolMap);
        String generatedFunctionExpression = CommonKeys.FUNCTION_KEYWORD_KEY + " " + functionName +
                CommonKeys.OPEN_PARENTHESES_KEY + parameterName + CommonKeys.CLOSE_PARENTHESES_KEY + " " +
                "returns " + returnSignature + CommonKeys.ARROW_FUNCTION_SYMBOL_KEY + " " + CommonKeys.OPEN_BRACE_KEY +
                System.lineSeparator() + "    " + editText + System.lineSeparator() +
                CommonKeys.CLOSE_BRACE_KEY + CommonKeys.SEMI_COLON_SYMBOL_KEY;
        String functionCall =
                functionName + CommonKeys.OPEN_PARENTHESES_KEY +
                        posDetails.matchedCodeActionNode().toSourceCode().stripTrailing() +
                        CommonKeys.CLOSE_PARENTHESES_KEY;
        try {
            generatedFunctionExpression = Formatter.format(generatedFunctionExpression);
        } catch (FormatterException e) {
            return Collections.emptyList();
        }
        generatedFunctionExpression =
                CommonUtil.LINE_SEPARATOR + CommonUtil.LINE_SEPARATOR + generatedFunctionExpression;
        textEdits.add(new TextEdit(PositionUtil.toRange(enclosingNode.get().lineRange().endLine()),
                generatedFunctionExpression));
        textEdits.add(new TextEdit(PositionUtil.toRange(posDetails.matchedCodeActionNode().lineRange()), functionCall));

        CodeAction codeAction =
                CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_TRANSFORM_FUNCTION, textEdits,
                        context.fileUri(),
                        CodeActionKind.RefactorExtract);
        CodeActionUtil.addRenamePopup(context, codeAction, CommandConstants.RENAME_COMMAND_TITLE_FOR_FUNCTION,
                textEdits.get(1).getRange().getStart());
        return Collections.singletonList(codeAction);
    }

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(
                SyntaxKind.FIELD_ACCESS,
                SyntaxKind.OPTIONAL_FIELD_ACCESS,
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
        return positionDetails.matchedTopLevelTypeSymbol().typeKind() == TypeDescKind.TYPE_REFERENCE &&
                positionDetails.matchedCodeActionNode().parent().kind() == SyntaxKind.SPECIFIC_FIELD &&
                CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    private static RecordTypeWrapper getReferredRecordSymbol(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE) {
            return null;
        }

        TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
        Optional<String> name = typeReferenceTypeSymbol.getName();
        if (name.isEmpty()) {
            return null;
        }

        TypeSymbol typeDescriptorTypeSymbol = typeReferenceTypeSymbol.typeDescriptor();
        if (typeDescriptorTypeSymbol.typeKind() != TypeDescKind.RECORD) {
            return null;
        }

        return new RecordTypeWrapper((RecordTypeSymbol) typeDescriptorTypeSymbol, name.get());
    }

    @Override
    public String getName() {
        return NAME;
    }

    private record RecordTypeWrapper(RecordTypeSymbol recordTypeSymbol, String recordTypeName) {

    }
}
