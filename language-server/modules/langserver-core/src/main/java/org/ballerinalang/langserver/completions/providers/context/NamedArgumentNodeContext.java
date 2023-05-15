/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Completion provider for {@link NamedArgumentNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class NamedArgumentNodeContext extends AbstractCompletionProvider<NamedArgumentNode> {

    public NamedArgumentNodeContext() {
        super(NamedArgumentNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, NamedArgumentNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, node.expression())) {
            /*
            Captures the following cases
            (1) arg1 = module:<cursor>
            (2) arg1 = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                    || symbol.kind() == SymbolKind.FUNCTION                     
                    || symbol.kind() == SymbolKind.TYPE_DEFINITION
                    || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, filter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Captures the following cases
            (1) arg1 = <cursor>
            (2) arg2 c = a<cursor>
             */
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(this.getNewExprCompletionItems(context, node));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, NamedArgumentNode node) {
        /*
        Following cases are considered to be the named arg context.
        (1) func(arg1=<cursor>)
        (2) func(arg2= m<cursor>)
         */
        int cursor = context.getCursorPositionInTree();
        TextRange textRange = node.expression().textRange();
        return !node.equalsToken().isMissing() && cursor >= node.equalsToken().textRange().endOffset()
                && cursor <= textRange.endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context, NamedArgumentNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> typeSymbol = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.location().lineRange().endLine();
            typeSymbol = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }

        if (typeSymbol.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        TypeSymbol symbol = typeSymbol.get();
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, symbol));
        }
    }

    private List<LSCompletionItem> getNewExprCompletionItems(BallerinaCompletionContext context,
                                                             NamedArgumentNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> type = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.location().lineRange().endLine();
            type = context.currentSemanticModel().get().expectedType(context.currentDocument().get(), linePosition);
        }

        if (type.isEmpty()) {
            return completionItems;
        }
        TypeSymbol rawType = CommonUtil.getRawType(type.get());
        if (rawType.kind() == SymbolKind.CLASS) {
            completionItems.add(this.getImplicitNewCItemForClass((ClassSymbol) rawType, context));
        }
        return completionItems;
    }
}
