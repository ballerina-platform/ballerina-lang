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
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;

/**
 * Completion provider for {@link WaitFieldsListNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WaitFieldsListNodeContext extends MappingContextProvider<WaitFieldsListNode> {

    public WaitFieldsListNodeContext() {
        super(WaitFieldsListNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WaitFieldsListNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        NonTerminalNode evalNode = getEvalNode(context);

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
            completionItems.addAll(this.getFieldCompletionItems(context, node, evalNode));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    protected boolean withinValueExpression(BallerinaCompletionContext context, NonTerminalNode evalNodeAtCursor) {
        if (evalNodeAtCursor.kind() != SyntaxKind.WAIT_FIELD) {
            return false;
        }
        Token colon = ((WaitFieldNode) evalNodeAtCursor).colon();
        int cursorPosInTree = context.getCursorPositionInTree();
        int colonStart = colon.textRange().startOffset();
        return cursorPosInTree > colonStart;
    }
    
    @Override
    protected Map<String, RecordFieldSymbol> getValidFields(WaitFieldsListNode node,
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

    @Override
    protected Predicate<Symbol> getVariableFilter() {
        return (symbol -> (symbol.kind() == SymbolKind.VARIABLE
                        && ((VariableSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.FUTURE)
        || symbol.kind() == SymbolKind.WORKER);
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
