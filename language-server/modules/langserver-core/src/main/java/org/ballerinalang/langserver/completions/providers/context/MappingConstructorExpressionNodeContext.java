/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link MappingConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MappingConstructorExpressionNodeContext extends
        AbstractCompletionProvider<MappingConstructorExpressionNode> {

    public MappingConstructorExpressionNodeContext() {
        super(MappingConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 MappingConstructorExpressionNode node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        NonTerminalNode evalNode = (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE
                || nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE)
                ? nodeAtCursor.parent() : nodeAtCursor;

        if (this.withinValueExpression(context, evalNode)) {
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(this.expressionCompletions(context));
            }
        } else if (this.withinComputedNameContext(context, evalNode)) {
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(getComputedNameCompletions(context));
            }
        } else {
            if (!this.hasReadonlyKW(evalNode)) {
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_READONLY.get()));
            }
            List<Pair<TypeSymbol, TypeSymbol>> recordTypeDesc = this.getRecordTypeDescs(context, node);
            for (Pair<TypeSymbol, TypeSymbol> recordTypeSymbol : recordTypeDesc) {
                RecordTypeSymbol rawType = (RecordTypeSymbol) (CommonUtil.getRawType(recordTypeSymbol.getLeft()));
                Map<String, RecordFieldSymbol> fields = this.getValidFields(node, rawType);
                // TODO: Revamp the implementation
                // completionItems.addAll(BLangRecordLiteralUtil.getSpreadCompletionItems(context, recordType));
                completionItems.addAll(CommonUtil.getRecordFieldCompletionItems(context, fields, recordTypeSymbol));
                if (!fields.values().isEmpty()) {
                    completionItems.add(CommonUtil.getFillAllStructFieldsItem(context, fields,
                            recordTypeSymbol));
                }
                completionItems.addAll(this.getVariableCompletionsForFields(context, fields));
            }
            if (recordTypeDesc.isEmpty()) {
                /*
                This means that we are within a mapping constructor for a map. Therefore we suggest the variables
                Eg: 
                function init() {
                    int test = 12;
                    map<string> myVar = {<cursor>};
                }
                 */
                List<Symbol> variables = context.visibleSymbols(context.getCursorPosition()).stream()
                        .filter(this.getVariableFilter())
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(variables, context));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MappingConstructorExpressionNode node) {
        return !node.openBrace().isMissing() && !node.closeBrace().isMissing();
    }

    private boolean withinValueExpression(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        Token colon = null;

        if (evalNodeAtCursor.kind() == SyntaxKind.SPECIFIC_FIELD) {
            colon = ((SpecificFieldNode) evalNodeAtCursor).colon().orElse(null);
        } else if (evalNodeAtCursor.kind() == SyntaxKind.COMPUTED_NAME_FIELD) {
            colon = ((ComputedNameFieldNode) evalNodeAtCursor).colonToken();
        }

        if (colon == null) {
            return false;
        }

        int cursorPosInTree = context.getCursorPositionInTree();
        int colonStart = colon.textRange().startOffset();

        return cursorPosInTree > colonStart;
    }

    private boolean withinComputedNameContext(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        if (evalNodeAtCursor.kind() != SyntaxKind.COMPUTED_NAME_FIELD) {
            return false;
        }

        int openBracketEnd = ((ComputedNameFieldNode) evalNodeAtCursor).openBracket().textRange().endOffset();
        int closeBracketStart = ((ComputedNameFieldNode) evalNodeAtCursor).closeBracket().textRange().startOffset();
        int cursorPosInTree = context.getCursorPositionInTree();

        return cursorPosInTree >= openBracketEnd && cursorPosInTree <= closeBracketStart;
    }

    private List<Pair<TypeSymbol, TypeSymbol>> getRecordTypeDescs(BallerinaCompletionContext context,
                                                                  MappingConstructorExpressionNode node) {
        ContextTypeResolver typeResolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> resolvedType = node.apply(typeResolver);
        if (resolvedType.isEmpty()) {
            return Collections.emptyList();
        }
        TypeSymbol rawType = CommonUtil.getRawType(resolvedType.get());
        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return Collections.singletonList(Pair.of(rawType, resolvedType.get()));
        }
        if (rawType.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                    .filter(typeSymbol -> CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.RECORD)
                    .map(typeSymbol -> Pair.of(CommonUtil.getRawType(typeSymbol), typeSymbol))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private List<LSCompletionItem> getVariableCompletionsForFields(BallerinaCompletionContext ctx,
                                                                   Map<String, RecordFieldSymbol> recFields) {
        List<Symbol> visibleSymbols = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                .filter(this.getVariableFilter().and(symbol -> {
                    Optional<TypeSymbol> typeDescriptor = SymbolUtil.getTypeDescriptor(symbol);
                    Optional<String> symbolName = symbol.getName();
                    return symbolName.isPresent() && typeDescriptor.isPresent()
                            && recFields.containsKey(symbolName.get())
                            && recFields.get(symbolName.get()).typeDescriptor().typeKind()
                            == typeDescriptor.get().typeKind();
                }))
                .collect(Collectors.toList());
        
        return this.getCompletionItemList(visibleSymbols, ctx);
    }

    private List<LSCompletionItem> getComputedNameCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());

        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol instanceof VariableSymbol || symbol.kind() == SymbolKind.FUNCTION)
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getCompletionItemList(filteredList, context);
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    private boolean hasReadonlyKW(NonTerminalNode evalNodeAtCursor) {
        return ((evalNodeAtCursor.kind() == SyntaxKind.SPECIFIC_FIELD)
                && ((SpecificFieldNode) evalNodeAtCursor).readonlyKeyword().isPresent());
    }

    private List<LSCompletionItem> getExpressionsCompletionsForQNameRef(BallerinaCompletionContext context,
                                                                        QualifiedNameReferenceNode qNameRef) {
        Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                || symbol.kind() == SymbolKind.FUNCTION;
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);

        return this.getCompletionItemList(moduleContent, context);
    }

    private Map<String, RecordFieldSymbol> getValidFields(MappingConstructorExpressionNode node,
                                                          RecordTypeSymbol recordTypeSymbol) {
        List<String> missingFields = node.fields().stream()
                .filter(field -> !field.isMissing() && field.kind() == SyntaxKind.SPECIFIC_FIELD
                        && ((SpecificFieldNode) field).fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(field -> ((IdentifierToken) ((SpecificFieldNode) field).fieldName()).text())
                .collect(Collectors.toList());
        Map<String, RecordFieldSymbol> fieldSymbols = new HashMap<>();
        recordTypeSymbol.fieldDescriptors().forEach((name, symbol) -> {
            if (!missingFields.contains(name)) {
                fieldSymbols.put(name, symbol);
            }
        });

        return fieldSymbols;
    }

    private Predicate<Symbol> getVariableFilter() {
        return CommonUtil.getVariableFilterPredicate().or(symbol -> symbol.kind() == SymbolKind.CONSTANT);
    }
}
