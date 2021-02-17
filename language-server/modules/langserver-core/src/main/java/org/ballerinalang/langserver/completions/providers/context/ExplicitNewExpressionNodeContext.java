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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ExplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ExplicitNewExpressionNodeContext extends AbstractCompletionProvider<ExplicitNewExpressionNode> {

    public ExplicitNewExpressionNodeContext() {
        super(ExplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ExplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        TypeDescriptorNode typeDescriptor = node.typeDescriptor();

        // TODO During the sorting we have to consider the LHS/ parent of the node
        if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            /*
            Supports the following
            (1) new <cursor>
            (2) new a<cursor>
             */
            List<ClassSymbol> filteredList = visibleSymbols.stream()
                    .filter(SymbolUtil::isClass)
                    .map(SymbolUtil::getTypeDescForClassSymbol)
                    .collect(Collectors.toList());
            for (ClassSymbol classSymbol : filteredList) {
                completionItems.add(this.getExplicitNewCompletionItem(classSymbol, context));
            }
            completionItems.addAll(this.getModuleCompletionItems(context));
        } else if (this.onQualifiedNameIdentifier(context, typeDescriptor)) {
            /*
            Supports the following
            (1) new module:<cursor>
            (2) new module:a<cursor>
             */
            QualifiedNameReferenceNode referenceNode = (QualifiedNameReferenceNode) typeDescriptor;
            String moduleName = QNameReferenceUtil.getAlias(referenceNode);
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, moduleName);
            if (module.isEmpty()) {
                return completionItems;
            }
            module.get().allSymbols().stream()
                    .filter(this.getModuleContentFilter(node))
                    .map(symbol -> (ClassSymbol) symbol)
                    .forEach(symbol -> completionItems.add(this.getExplicitNewCompletionItem(symbol, context)));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }
    
    private Predicate<Symbol> getModuleContentFilter(ExplicitNewExpressionNode node) {
        if (node.parent().kind() == SyntaxKind.SERVICE_DECLARATION
                || node.parent().kind() == SyntaxKind.LISTENER_DECLARATION) {
            return symbol -> symbol.kind() == SymbolKind.CLASS && SymbolUtil.isListener(symbol);
        }
        
        return symbol -> symbol.kind() == SymbolKind.CLASS;
    }
}
