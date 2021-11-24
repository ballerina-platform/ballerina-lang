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
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.VariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;

/**
 * Completion provider for {@link WaitFieldsListNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WaitFieldsListNodeContext extends AbstractCompletionProvider<WaitFieldsListNode> {

    public WaitFieldsListNodeContext() {
        super(WaitFieldsListNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WaitFieldsListNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        NonTerminalNode evalNode = (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE
                || nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE)
                ? nodeAtCursor.parent() : nodeAtCursor;

        if (this.withinValueExpression(context, evalNode)) {
                /*
            Captures the following cases.
            (1) wait {fieldName: <cursor>}
            (2) wait {fieldName: a<cursor>}
             */
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(this.expressionCompletions(context));
            }
        } else {
            /*
            Captures the following cases.
            (1) wait {<cursor>}
            (2) wait {fieldName: value, <cursor>}
             */
            List<Pair<TypeSymbol, TypeSymbol>> recordTypeDesc = this.getRecordTypeDescs(context, node);
            for (Pair<TypeSymbol, TypeSymbol> recordTypeSymbol : recordTypeDesc) {
                RecordTypeSymbol rawType = (RecordTypeSymbol) (CommonUtil.getRawType(recordTypeSymbol.getLeft()));
                Map<String, RecordFieldSymbol> fields = this.getValidFields(node, rawType);
                completionItems.addAll(CommonUtil.getRecordFieldCompletionItems(context, fields, recordTypeSymbol));
                if (!fields.values().isEmpty()) {
                    completionItems.add(CommonUtil.getFillAllStructFieldsItem(context, fields,
                            recordTypeSymbol));
                }
                completionItems.addAll(this.getVariableCompletionsForFields(context, fields));
            }

            if (recordTypeDesc.isEmpty()) {
                /*
                Captures the following case within a mapping constructor for a map.
                Eg: 
                function init() {
                    worker WA returns string { return ""; }
                    map<string> myVar = wait {<cursor>};
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

    private List<LSCompletionItem> getExpressionsCompletionsForQNameRef(BallerinaCompletionContext context,
                                                                        QualifiedNameReferenceNode qNameRef) {
        Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                || symbol.kind() == SymbolKind.FUNCTION;
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);

        return this.getCompletionItemList(moduleContent, context);
    }

    private List<Pair<TypeSymbol, TypeSymbol>> getRecordTypeDescs(BallerinaCompletionContext context,
                                                                  WaitFieldsListNode node) {
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

    private Map<String, RecordFieldSymbol> getValidFields(WaitFieldsListNode node,
                                                          RecordTypeSymbol recordTypeSymbol) {
        List<String> missingFields = node.waitFields().stream()
                .filter(field -> !field.isMissing() && field.kind() == SyntaxKind.WAIT_FIELD
                        && ((WaitFieldNode) field).fieldName().name().kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(field -> (((WaitFieldNode) field).fieldName().name()).text())
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
        return (symbol -> (symbol.kind() == SymbolKind.VARIABLE
                        && ((VariableSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.FUTURE)
        || symbol.kind() == SymbolKind.WORKER);
    }

    private List<LSCompletionItem> getVariableCompletionsForFields(BallerinaCompletionContext ctx,
                                                                   Map<String, RecordFieldSymbol> recFields) {
        List<Symbol> visibleSymbols = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                .filter(this.getVariableFilter())
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(symbol -> {
            TypeSymbol typeDescriptor;
            String symbolName = symbol.getName().get();
            if (symbol.kind() == VARIABLE) {
                typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                if (recFields.containsKey(symbolName)
                        && recFields.get(symbolName).typeDescriptor().kind() == typeDescriptor.kind()) {
                    CompletionItem cItem = VariableCompletionItemBuilder.build((VariableSymbol) symbol, symbolName,
                            CommonUtil.getModifiedTypeName(ctx, typeDescriptor));
                    completionItems.add(new SymbolCompletionItem(ctx, symbol, cItem));
                }
            } else if (symbol.kind() == WORKER) {
                typeDescriptor = ((WorkerSymbol) symbol).returnType();
                if (recFields.containsKey(symbolName)
                        && recFields.get(symbolName).typeDescriptor().signature().equals(typeDescriptor.signature())) {
                    CompletionItem cItem = VariableCompletionItemBuilder.build((WorkerSymbol) symbol, symbolName,
                            CommonUtil.getModifiedTypeName(ctx, typeDescriptor));
                    completionItems.add(new SymbolCompletionItem(ctx, symbol, cItem));
                }
            }
        });

        return completionItems;
    }

    private boolean withinValueExpression(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        Token colon = null;

        if (evalNodeAtCursor.kind() == SyntaxKind.WAIT_FIELD) {
            colon = ((WaitFieldNode) evalNodeAtCursor).colon();
        } 

        if (colon == null) {
            return false;
        }
        int cursorPosInTree = context.getCursorPositionInTree();
        int colonStart = colon.textRange().startOffset();

        return cursorPosInTree > colonStart;
    }
    
    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));

        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> (symbol instanceof VariableSymbol || symbol.kind() == PARAMETER ||
                        symbol.kind() == FUNCTION || symbol.kind() == WORKER)
                        && !symbol.getName().orElse("").equals(Names.ERROR.getValue()))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        return completionItems;
    }
}
