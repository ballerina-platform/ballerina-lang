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

import io.ballerinalang.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ArrayTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ArrayTypeDescriptorNodeContext extends AbstractCompletionProvider<ArrayTypeDescriptorNode> {

    public ArrayTypeDescriptorNodeContext() {
        super(ArrayTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ArrayTypeDescriptorNode node) {

        List<Scope.ScopeEntry> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<Node> arrayLength = node.arrayLength();
        if (arrayLength.isPresent() && this.onQualifiedNameIdentifier(context, arrayLength.get())) {
            Predicate<Scope.ScopeEntry> predicate = scopeEntry -> scopeEntry.symbol instanceof BConstantSymbol
                    && ((BConstantSymbol) scopeEntry.symbol).literalType.getKind() == TypeKind.INT
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) arrayLength.get();
            List<Scope.ScopeEntry> moduleConstants = QNameReferenceUtil.getModuleContent(context, qNameRef, predicate);

            return this.getCompletionItemList(moduleConstants, context);
        }

        List<Scope.ScopeEntry> constants = visibleSymbols.stream()
                .filter(constantFilterPredicate())
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getPackagesCompletionItems(context);
        completionItems.addAll(this.getCompletionItemList(constants, context));

        return completionItems;
    }

    private Predicate<Scope.ScopeEntry> constantFilterPredicate() {
        return scopeEntry -> scopeEntry.symbol instanceof BConstantSymbol
                && ((BConstantSymbol) scopeEntry.symbol).literalType.getKind() == TypeKind.INT;
    }
}
