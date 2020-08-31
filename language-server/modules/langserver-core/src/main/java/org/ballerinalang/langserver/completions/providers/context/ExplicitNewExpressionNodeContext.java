/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ExplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ExplicitNewExpressionNodeContext extends AbstractCompletionProvider<ExplicitNewExpressionNode> {

    public ExplicitNewExpressionNodeContext() {
        super(ExplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        return new ArrayList<>();
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ExplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        TypeDescriptorNode typeDescriptor = node.typeDescriptor();

        if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            List<Scope.ScopeEntry> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
            /*
            Supports the following
            (1) public listener mod:Listener test = new <cursor>
            (2) public listener mod:Listener test = new a<cursor>
             */
            List<BObjectTypeSymbol> filteredList = visibleSymbols.stream()
                    .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                    .map(scopeEntry -> (BObjectTypeSymbol) scopeEntry.symbol)
                    .collect(Collectors.toList());
            for (BObjectTypeSymbol objectTypeSymbol : filteredList) {
                completionItems.add(this.getExplicitNewCompletionItem(objectTypeSymbol, context));
            }
            completionItems.addAll(this.getPackagesCompletionItems(context));
        } else if (this.onQualifiedNameIdentifier(context, typeDescriptor)) {
            QualifiedNameReferenceNode referenceNode = (QualifiedNameReferenceNode) typeDescriptor;
            String moduleName = QNameReferenceUtil.getAlias(referenceNode);
            Optional<Scope.ScopeEntry> module = CommonUtil.packageSymbolFromAlias(context, moduleName);
            if (!module.isPresent()) {
                return completionItems;
            }
            List<BObjectTypeSymbol> filteredList = module.get().symbol.scope.entries.values().stream()
                    .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                    .map(scopeEntry -> (BObjectTypeSymbol) scopeEntry.symbol)
                    .collect(Collectors.toList());
            for (BObjectTypeSymbol objectTypeSymbol : filteredList) {
                completionItems.add(this.getExplicitNewCompletionItem(objectTypeSymbol, context));
            }
        }

        return completionItems;
    }
}
