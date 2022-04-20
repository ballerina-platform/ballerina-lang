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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
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

/**
 * Completion provider for {@link AnnotationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TableTypeDescriptorNodeContext extends AbstractCompletionProvider<TableTypeDescriptorNode> {

    public TableTypeDescriptorNodeContext() {
        super(TableTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TableTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onSuggestKeyKw(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_KEY.get()));
        }
        // Sorting is invoked to maintain consistency and avoid missing the phase in future with new modifications
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onSuggestKeyKw(BallerinaCompletionContext context, TableTypeDescriptorNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<Node> keyConstraint = node.keyConstraintNode();
        Node rowTypeParamNode = node.rowTypeParameterNode();

        boolean shouldSuggest = (keyConstraint.isEmpty() && cursor >= rowTypeParamNode.textRange().endOffset())
                || (keyConstraint.isPresent() && cursor <= keyConstraint.get().textRange().startOffset()
                && cursor >= rowTypeParamNode.textRange().endOffset());

        if (!shouldSuggest || node.rowTypeParameterNode().kind() != SyntaxKind.TYPE_PARAMETER) {
            return false;
        }

        TypeParameterNode typeParameterNode = (TypeParameterNode) node.rowTypeParameterNode();
        // Get type of type parameter
        Optional<Symbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(typeParameterNode.typeNode()));
        if (symbol.isEmpty()) {
            return false;
        }

        TypeSymbol typeSymbol = CommonUtil.getRawType((TypeSymbol) symbol.get());
        // key specifier or key constraint not allowed for map types
        return typeSymbol.typeKind() == TypeDescKind.RECORD;
    }
}
