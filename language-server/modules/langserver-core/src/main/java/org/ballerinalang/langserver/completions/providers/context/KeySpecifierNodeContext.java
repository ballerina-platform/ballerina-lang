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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.RecordFieldCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link KeySpecifierNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class KeySpecifierNodeContext extends AbstractCompletionProvider<KeySpecifierNode> {

    public KeySpecifierNodeContext() {
        super(KeySpecifierNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, KeySpecifierNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = getKeyCompletionItems(context, node);
        this.sort(context, node, completionItems);
        return completionItems;
    }

    private List<LSCompletionItem> getKeyCompletionItems(BallerinaCompletionContext context,
                                                         KeySpecifierNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        TypeSymbol rowTypeSymbol;
        if (node.parent().kind() == SyntaxKind.TABLE_TYPE_DESC) {
            TableTypeDescriptorNode tableTypeDef = (TableTypeDescriptorNode) node.parent();
            if (tableTypeDef.rowTypeParameterNode().kind() != SyntaxKind.TYPE_PARAMETER) {
                return completionItems;
            }
            TypeParameterNode typeParameterNode = (TypeParameterNode) tableTypeDef.rowTypeParameterNode();
            // Get type of type parameter
            Optional<TypeSymbol> typeSymbol = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.symbol(typeParameterNode.typeNode()))
                    .flatMap(SymbolUtil::getTypeDescriptor);
            if (typeSymbol.isEmpty()) {
                return completionItems;
            }
            rowTypeSymbol = typeSymbol.get();
        } else {
            //Note: There is not a way to get the key constraint atm. 
            // If the key constraint specifies only a single basic type,
            // we should only suggest specifiers of that type.
            Optional<TypeSymbol> typeSymbol = context.getContextType();
            if (typeSymbol.isEmpty()) {
                return completionItems;
            }
            rowTypeSymbol = CommonUtil.getRawType(typeSymbol.get());
        }
        if (rowTypeSymbol.typeKind() != TypeDescKind.RECORD) {
            return completionItems;
        }
        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) rowTypeSymbol;
        // Get existing keys
        Set<String> fieldNames = node.fieldNames().stream()
                .filter(identifierToken -> !identifierToken.isMissing())
                .map(Token::text)
                .collect(Collectors.toSet());
        // Get field symbols which are not already specified
        List<RecordFieldSymbol> symbols = recordTypeSymbol.fieldDescriptors().values().stream()
                .filter(recordFieldSymbol -> recordFieldSymbol.getName().isPresent()
                        && CommonUtil.isValidIdentifier(recordFieldSymbol.getName().get())
                        && !fieldNames.contains(recordFieldSymbol.getName().get()))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(symbols, context));
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, KeySpecifierNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem completionItem : completionItems) {
            int rank = 2;
            if (completionItem.getType() == LSCompletionItem.CompletionItemType.RECORD_FIELD) {
                RecordFieldSymbol recordFieldSymbol = ((RecordFieldCompletionItem) completionItem).getFieldSymbol();
                if (recordFieldSymbol.qualifiers().contains(Qualifier.READONLY)) {
                    rank = 1;
                }
            }
            completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank)
                    + SortingUtil.genSortText(SortingUtil.toRank(context, completionItem)));
        }
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, KeySpecifierNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.keyKeyword().isMissing() &&
                node.openParenToken().textRange().startOffset() <= cursor &&
                (node.closeParenToken().isMissing() ||
                        cursor < node.closeParenToken().textRange().endOffset());
    }
}
