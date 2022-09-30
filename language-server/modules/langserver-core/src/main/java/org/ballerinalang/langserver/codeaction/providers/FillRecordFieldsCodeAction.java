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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionContextTypeResolver;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.RawTypeSymbolWrapper;
import org.ballerinalang.langserver.common.utils.RecordUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code Action to autofill record fields.
 *
 * @since 2201.3.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class FillRecordFieldsCodeAction implements RangeBasedCodeActionProvider {
    public static final String NAME = "Fill Record Fields";
    public static final String DIAGNOSTIC_CODE = "BCE2520";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.STRING_LITERAL, SyntaxKind.NUMERIC_LITERAL,
                SyntaxKind.BINARY_EXPRESSION, SyntaxKind.UNARY_EXPRESSION);
    }

    @Override
    public boolean validate(CodeActionContext context,
                            RangeBasedPositionDetails positionDetails) {
        return context.diagnostics(context.filePath()).stream().filter(diag -> PositionUtil
                .isRangeWithinRange(context.range(), PositionUtil.toRange(diag.location().lineRange())))
                .anyMatch(diagnostic -> diagnostic.diagnosticInfo().code().equals(DIAGNOSTIC_CODE))
                && positionDetails.matchedCodeActionNode().apply(new ExtendsCodeActionNodeValidator());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        Node node = context.nodeAtRange();
        Optional<Node> evalNode = CommonUtil.getEvalNode(node);
        if (evalNode.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<Node> mappingConstructorNode = getMappingConstructorNode(evalNode.get());
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (mappingConstructorNode.isEmpty() || semanticModel.isEmpty()) {
            return Collections.emptyList();
        }
        CodeActionContextTypeResolver contextTypeResolver = new CodeActionContextTypeResolver(context);
        Optional<TypeSymbol> typeSymbol = mappingConstructorNode.get().apply(contextTypeResolver);
        if (typeSymbol == null || typeSymbol.isEmpty()) {
            return Collections.emptyList();
        }
        List<RawTypeSymbolWrapper<RecordTypeSymbol>> recordTypeSymbols =
                RecordUtil.getRecordTypeSymbols(typeSymbol.get());
        MappingConstructorExpressionNode expressionNode =
                (MappingConstructorExpressionNode) mappingConstructorNode.get();

        List<String> existingFields = getFields(expressionNode);
        List<RecordFieldSymbol> validFields = new ArrayList<>();
        Map<String, RecordFieldSymbol> fields = new HashMap<>();
        String detail = "";
        for (RawTypeSymbolWrapper<RecordTypeSymbol> symbol : recordTypeSymbols) {
            fields = RecordUtil.getRecordFields(symbol, existingFields);
            validFields.addAll(fields.values());
            detail = NameUtil.getRecordType(context, symbol);
        }
        if (validFields.isEmpty() && fields.values().isEmpty()) {
            return Collections.emptyList();
        }

        String editText;
        if (expressionNode.fields().isEmpty() || expressionNode.toSourceCode().replaceAll("[{}]", "")
                .replaceAll("\\s", "").endsWith(",")) {
            editText = RecordUtil.getFillAllRecordFieldInsertText(fields);
        } else {
            editText = "," + RecordUtil.getFillAllRecordFieldInsertText(fields);
        }

        LinePosition linePosition = expressionNode.closeBrace().lineRange().startLine();
        Position position = PositionUtil.toPosition(linePosition);
        TextEdit textEdit = new TextEdit(new Range(position, position), editText);
        String commandTitle = String.format(CommandConstants.FILL_REQUIRED_FIELDS, detail);
        return Collections.singletonList(CodeActionUtil.createCodeAction(commandTitle, List.of(textEdit),
                context.fileUri(), CodeActionKind.QuickFix));
    }

    protected List<String> getFields(MappingConstructorExpressionNode node) {
        return node.fields().stream()
                .filter(field -> !field.isMissing() && field.kind() == SyntaxKind.SPECIFIC_FIELD
                        && ((SpecificFieldNode) field).fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(field -> ((IdentifierToken) ((SpecificFieldNode) field).fieldName()).text())
                .collect(Collectors.toList());
    }

    private static Optional<Node> getMappingConstructorNode(Node node) {
        while (node.kind() != SyntaxKind.MAPPING_CONSTRUCTOR) {
            node = node.parent();
        }
        return Optional.of(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    static class ExtendsCodeActionNodeValidator extends CodeActionNodeValidator {

        @Override
        public Boolean transform(SpecificFieldNode node) {
            return super.transform(node) || (node.colon().isEmpty() && node.fieldName().toSourceCode().isEmpty()
                    && node.valueExpr().isEmpty()) || (!node.fieldName().toSourceCode().isEmpty() &&
                    node.colon().isEmpty());
        }
    }
}
