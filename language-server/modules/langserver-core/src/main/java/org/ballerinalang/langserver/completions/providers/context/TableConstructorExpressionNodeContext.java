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
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link TableConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TableConstructorExpressionNodeContext extends AbstractCompletionProvider<TableConstructorExpressionNode> {

    public TableConstructorExpressionNodeContext() {
        super(TableConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, TableConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        int cursor = ctx.getCursorPositionInTree();

        if (this.onKeySpecifier(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_KEY.get()));
        } else if (withInKeySpecifier(ctx, node)) {
            completionItems.addAll(getKeyCompletionItems(ctx, node));
        } else if (node.keySpecifier().isPresent() && node.keySpecifier().get().textRange().endOffset() < cursor) {
            /*
            Covers the following
            (1) var test = table key(id) f<cursor>
            (2) var test = stream f<cursor>
            This particular section hits only when (1) and (2) are being the last statement of a block (ex: in function)
             */
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_FROM.get()));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.CLAUSE_FROM.get()));
        }
        this.sort(ctx, node, completionItems);

        return completionItems;
    }

    private boolean onKeySpecifier(BallerinaCompletionContext context, TableConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<KeySpecifierNode> keySpecifier = node.keySpecifier();
        Token tableKeyword = node.tableKeyword();

        return cursor > tableKeyword.textRange().endOffset()
                && (keySpecifier.isEmpty() || cursor < keySpecifier.get().keyKeyword().textRange().startOffset());
    }

    private boolean withInKeySpecifier(BallerinaCompletionContext context, TableConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<KeySpecifierNode> keySpecifier = node.keySpecifier();
        return keySpecifier.isPresent() && keySpecifier.get().textRange().startOffset() <= cursor
                && cursor <= keySpecifier.get().textRange().endOffset();
    }

    private List<LSCompletionItem> getKeyCompletionItems(BallerinaCompletionContext context,
                                                         TableConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> typeSymbol = context.getContextType();
        if (typeSymbol.isEmpty()) {
            return completionItems;
        }
        TypeSymbol rawTypeSymbol = CommonUtil.getRawType(typeSymbol.get());
        if (rawTypeSymbol.typeKind() != TypeDescKind.RECORD) {
            return completionItems;
        }
        //Note: There is not a way to get the key constraint atm. 
        // If the key constraint specifies only a single basic type,
        // we should only suggest specifiers of that type.
        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) rawTypeSymbol;
        // Get existing keys
        Optional<KeySpecifierNode> keySpecifierNode = node.keySpecifier();
        if (keySpecifierNode.isEmpty()) {
            return completionItems;
        }
        Set<String> fieldNames = keySpecifierNode.get().fieldNames().stream()
                .filter(identifierToken -> !identifierToken.isMissing())
                .map(Token::text)
                .collect(Collectors.toSet());
        // Get field symbols which are readonly and not already specified
        List<RecordFieldSymbol> symbols = recordTypeSymbol.fieldDescriptors().values().stream()
                .filter(recordFieldSymbol -> recordFieldSymbol.qualifiers().contains(Qualifier.READONLY))
                .filter(recordFieldSymbol -> recordFieldSymbol.getName().isPresent() &&
                        !fieldNames.contains(recordFieldSymbol.getName().get()))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(symbols, context));
        return completionItems;
    }
}
