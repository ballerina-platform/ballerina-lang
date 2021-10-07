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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.List;
import java.util.Optional;

/**
 * Abstract implementation of the field access and optional field access expression completion providers.
 *
 * @param <T> Field access node type
 * @since 2.0.0
 */
public abstract class AbstractFieldAccessExpressionNodeContext<T extends Node> extends FieldAccessContext<T> {

    public AbstractFieldAccessExpressionNodeContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     T node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem completionItem : completionItems) {
            int rank;
            switch (completionItem.getType()) {
                case OBJECT_FIELD:
                case RECORD_FIELD:
                    rank = 1;
                    break;
                case SYMBOL:
                    Optional<Symbol> symbol = ((SymbolCompletionItem) completionItem).getSymbol();
                    if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.XMLNS)) {
                        rank = 2;
                        break;
                    } else if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.METHOD)) {
                        rank = 3;
                        break;
                    } else if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.FUNCTION)) {
                        rank = 4;
                        break;
                    } else {
                        rank = SortingUtil.toRank(context, completionItem, 4);
                        break;
                    }
                default:
                    rank = SortingUtil.toRank(context, completionItem, 4);
            }

            sortByAssignability(context, completionItem, rank);
        }
    }
}
