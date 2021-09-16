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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Completion provider for {@link ModulePartNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ModulePartNodeContext extends AbstractCompletionProvider<ModulePartNode> {

    public ModulePartNodeContext() {
        super(ModulePartNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ModulePartNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        
        if (ModulePartNodeContextUtil.onServiceTypeDescContext(context.getTokenAtCursor(), context)) {
            /*
            Covers the following cases
            Eg:
            (1) service <cursor>
                function ....
            (2) isolated service <cursor>
                function ....
            
            Bellow cases are being handled by ModuleVariableDeclarationNodeContext
            Eg:
            (1) service m<cursor>
            (2) isolated service m<cursor>
             */
            List<Symbol> objectSymbols = ModulePartNodeContextUtil.serviceTypeDescContextSymbols(context);
            completionItems.addAll(this.getCompletionItemList(objectSymbols, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        } else if (onSuggestionsAfterQualifiers(context, node)) {
            /*
                Covers the following.
                <qualifier(s)> <cursor>
                currently the qualifier can be isolated/transactional/client.
            */
            completionItems.addAll(getCompletionItemsOnQualifiers(node, context));
        } else {
            completionItems.addAll(this.getModulePartContextItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private List<LSCompletionItem> getModulePartContextItems(BallerinaCompletionContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            Predicate<Symbol> predicate =
                    symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> types = QNameReferenceUtil.getModuleContent(context,
                    (QualifiedNameReferenceNode) nodeAtCursor, predicate);
            return this.getCompletionItemList(types, context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(ModulePartNodeContextUtil.getTopLevelItems(context));
        completionItems.addAll(this.getTypeDescContextItems(context));

        return completionItems;
    }


    @Override
    public void sort(BallerinaCompletionContext context, ModulePartNode node, List<LSCompletionItem> items) {
        ModulePartNodeContextUtil.sort(items);
    }
}
