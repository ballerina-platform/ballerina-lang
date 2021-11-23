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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.providers.context.util.ServiceTemplateGenerator;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            completionItems.addAll(this.getCompletionItemsOnQualifiers(node, context));
        } else if (onSuggestionsAfterQualifiers(context, node)
                && !QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            /*
                Covers the following.
                <qualifier> <cursor>
                function...
                currently the qualifier can be isolated/transactional/client.
            */
            completionItems.addAll(this.getCompletionItemsOnQualifiers(node, context));
        } else {
            completionItems.addAll(this.getModulePartContextItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        Set<SyntaxKind> qualKinds = qualifiers.stream().map(Node::kind).collect(Collectors.toSet());
        switch (lastQualifier.kind()) {
            case PUBLIC_KEYWORD:
                completionItems.addAll(getTypeDescContextItems(context));
                List<Snippet> snippets = Arrays.asList(
                        Snippet.KW_TYPE, Snippet.KW_ISOLATED,
                        Snippet.KW_FINAL, Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_CLIENT,
                        Snippet.KW_VAR, Snippet.KW_ENUM, Snippet.KW_XMLNS, Snippet.KW_CLASS,
                        Snippet.KW_TRANSACTIONAL, Snippet.DEF_FUNCTION, Snippet.DEF_MAIN_FUNCTION, 
                        Snippet.KW_CONFIGURABLE, Snippet.DEF_ANNOTATION,
                        Snippet.DEF_RECORD, Snippet.STMT_NAMESPACE_DECLARATION,
                        Snippet.DEF_OBJECT_SNIPPET, Snippet.DEF_CLASS, Snippet.DEF_ENUM, Snippet.DEF_CLOSED_RECORD,
                        Snippet.DEF_ERROR_TYPE, Snippet.DEF_TABLE_TYPE_DESC, Snippet.DEF_TABLE_WITH_KEY_TYPE_DESC,
                        Snippet.DEF_STREAM, Snippet.DEF_SERVICE_COMMON
                );
                snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));
                return completionItems;
            case SERVICE_KEYWORD:
            case CLIENT_KEYWORD:
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLASS.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLASS.get()));
                break;
            case ISOLATED_KEYWORD:
                if (qualKinds.contains(SyntaxKind.TRANSACTIONAL_KEYWORD)) {
                    completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
                    break;
                }
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLASS.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLASS.get()));
                if (!qualKinds.contains(SyntaxKind.SERVICE_KEYWORD) &&
                        !qualKinds.contains(SyntaxKind.CLIENT_KEYWORD)) {
                    /*
                        Covers the following.
                        isolated <cursor>
                     */
                    completionItems.addAll(this.getTypeDescContextItems(context));
                }
                break;
            case TRANSACTIONAL_KEYWORD:
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
                break;
            default:
        }
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
        completionItems.addAll(ServiceTemplateGenerator.getInstance(context.languageServercontext())
                .getServiceTemplates(context));
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ModulePartNode node, List<LSCompletionItem> items) {
        ModulePartNodeContextUtil.sort(items);
    }
}
