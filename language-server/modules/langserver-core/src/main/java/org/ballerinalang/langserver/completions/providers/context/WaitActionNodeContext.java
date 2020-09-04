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

import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.WaitActionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link WaitActionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class WaitActionNodeContext extends AbstractCompletionProvider<WaitActionNode> {

    public WaitActionNodeContext() {
        super(WaitActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, WaitActionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (!node.waitFutureExpr().isMissing() && node.waitFutureExpr().kind() == SyntaxKind.BINARY_EXPRESSION) {
            /*
            Covers the following,
            (1) wait fs1|f<cursor>
             */
            ArrayList<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
            List<Scope.ScopeEntry> filteredSymbols = visibleSymbols.stream().filter(scopeEntry -> {
                BSymbol symbol = scopeEntry.symbol;
                return symbol instanceof BVarSymbol && symbol.type instanceof BFutureType;
            }).collect(Collectors.toList());

            completionItems.addAll(this.getCompletionItemList(filteredSymbols, context));
        } else {
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
        }
        return completionItems;
    }
}
