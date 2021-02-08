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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.providers.context.util.ObjectConstructorBodyContextUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ServiceDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ServiceDeclarationNodeContext extends AbstractCompletionProvider<ServiceDeclarationNode> {

    public ServiceDeclarationNodeContext() {
        super(ServiceDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ServiceDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Token onKeyword = node.onKeyword();
        Position cursor = context.getCursorPosition();

        if (this.onMemberContext(context, node)) {
            completionItems.addAll(this.getMemberContextCompletions(context, node));
        } else if (this.onTypeDescContext(context, node)) {
            /*
            Covers the following cases
            Eg:
            (1) service m<cursor>
            function ...
            (2) service mod:<cursor>
            function ...
             */
            if (this.onQualifiedNameIdentifier(context, context.getTokenAtCursor().parent())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getTokenAtCursor().parent();
                List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef,
                        ModulePartNodeContextUtil.serviceTypeDescPredicate());

                return this.getCompletionItemList(moduleContent, context);
            }
            List<Symbol> typeDescs = ModulePartNodeContextUtil.serviceTypeDescContextSymbols(context);
            completionItems.addAll(this.getCompletionItemList(typeDescs, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        } else if (!onKeyword.isMissing() && cursor.getCharacter() > onKeyword.lineRange().endLine().offset()) {
            /*
            Covers the following case
            (1) service typedesc on <cursor>
            (2) service typedesc on l<cursor>
            (d) service typedesc on mod:<cursor>
             */
            Predicate<Symbol> predicate = symbol -> symbol instanceof VariableSymbol
                    && ((VariableSymbol) symbol).qualifiers().contains(Qualifier.LISTENER);
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context,
                        (QualifiedNameReferenceNode) nodeAtCursor, predicate);
                completionItems.addAll(this.getCompletionItemList(moduleContent, context));
            } else {
                List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
                List<Symbol> listeners = visibleSymbols.stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(listeners, context));
                completionItems.addAll(this.getModuleCompletionItems(context));
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
            }
        } else {
        
            /*
            Covers the following cases
            Eg:
            (1) service / m<cursor>
            function ...
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onTypeDescContext(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token onKeyword = node.onKeyword();
        Token serviceKeyword = node.serviceKeyword();
        Optional<TypeDescriptorNode> tDesc = node.typeDescriptor();
        NodeList<Node> resourcePath = node.absoluteResourcePath();
        boolean afterResourcePath = resourcePath.isEmpty()
                || cursor >= resourcePath.get(resourcePath.size() - 1).textRange().endOffset() + 1;
        boolean afterTypeDesc = tDesc.isEmpty() || cursor < tDesc.get().textRange().endOffset() + 1;
        boolean beforeOnKw = (onKeyword.isMissing() || onKeyword.textRange().startOffset() > cursor);

        return cursor > serviceKeyword.textRange().endOffset() && beforeOnKw && afterTypeDesc && afterResourcePath;
    }

    private boolean onMemberContext(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBrace = node.openBraceToken();
        Token closeBrace = node.closeBraceToken();

        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }

        return cursor > openBrace.textRange().startOffset() && cursor < closeBrace.textRange().endOffset();
    }

    private List<LSCompletionItem> getMemberContextCompletions(BallerinaCompletionContext context,
                                                               ServiceDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        completionItems.addAll(ObjectConstructorBodyContextUtil.getBodyContextSnippets(node, context));

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        /*
        Avoid picking the service declaration context in the following scenario
        p<cursor>
        service ...
         */
        int cursor = context.getCursorPositionInTree();
        Token serviceKeyword = node.serviceKeyword();

        return serviceKeyword.textRange().endOffset() < cursor;
    }
}
