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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link EnumMemberNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class EnumMemberNodeContext extends AbstractCompletionProvider<EnumMemberNode> {

    public EnumMemberNodeContext() {
        super(EnumMemberNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, EnumMemberNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        List<Symbol> visibleSymbols = new ArrayList<>();
        Predicate<Symbol> filter = symbol -> symbol.kind() == SymbolKind.CONSTANT
                && ((ConstantSymbol) symbol).broaderTypeDescriptor().typeKind() == TypeDescKind.STRING;
        if (!inEnumMemberValueContext(ctx, node)) {
            return completionItems;
        }
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            visibleSymbols.addAll(QNameRefCompletionUtil.getModuleContent(ctx, nameRef, filter));
        } else {
            completionItems.addAll(this.getModuleCompletionItems(ctx));
            List<Symbol> filteredSymbols = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            visibleSymbols.addAll(filteredSymbols);
        }
        completionItems.addAll(this.getCompletionItemList(visibleSymbols, ctx));
        this.sort(ctx, node, completionItems);
        
        return completionItems;
    }

    private boolean inEnumMemberValueContext(BallerinaCompletionContext ctx, EnumMemberNode node) {
        Optional<Token> equalToken = node.equalToken();
        if (equalToken.isEmpty()) {
            return false;
        }
        return equalToken.get().position() < ctx.getCursorPositionInTree();
    }
}
