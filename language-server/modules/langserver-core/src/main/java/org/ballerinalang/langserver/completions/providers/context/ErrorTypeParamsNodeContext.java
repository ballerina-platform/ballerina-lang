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

import io.ballerinalang.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ErrorTypeParamsNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ErrorTypeParamsNodeContext extends AbstractCompletionProvider<ErrorTypeParamsNode> {

    public ErrorTypeParamsNodeContext() {
        super(ErrorTypeParamsNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ErrorTypeParamsNode node) {
        /*
        Covers the following cases
        (1) error< <cursor> >
        (2) error< t<cursor> >
        (3) error< module:<cursor> >
        (4) error< module:t<cursor> >
         */
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        Predicate<Scope.ScopeEntry> predicate = scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            return symbol instanceof BTypeSymbol
                    && (symbol.type.getKind() == TypeKind.MAP || symbol.type.getKind() == TypeKind.RECORD);
        };
        List<Scope.ScopeEntry> mappingTypes;
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            mappingTypes = QNameReferenceUtil.getModuleContent(context, (QualifiedNameReferenceNode) nodeAtCursor,
                    predicate.and(scopeEntry -> (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC));
            return this.getCompletionItemList(mappingTypes, context);
        }

        List<Scope.ScopeEntry> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        mappingTypes = visibleSymbols.stream().filter(predicate).collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getCompletionItemList(mappingTypes, context);
        completionItems.addAll(this.getPackagesCompletionItems(context));

        return completionItems;
    }
}
