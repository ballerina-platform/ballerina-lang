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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ArrayTypeDescriptorNode} context.
 * 
 * Array type descriptor suggests the completions only if the cursor is within the brackets
 * eg:
 * (1) TypeName[[cursor]] ...
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ArrayTypeDescriptorNodeContext extends AbstractCompletionProvider<ArrayTypeDescriptorNode> {

    public ArrayTypeDescriptorNodeContext() {
        super(ArrayTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ArrayTypeDescriptorNode node) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qName = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> moduleConstants = QNameReferenceUtil.getModuleContent(context, qName, constantFilter());

            completionItems.addAll(this.getCompletionItemList(moduleConstants, context));
        } else {
            List<Symbol> constants = visibleSymbols.stream()
                    .filter(constantFilter())
                    .collect(Collectors.toList());
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getCompletionItemList(constants, context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Predicate<Symbol> constantFilter() {
        return symbol -> {
            if (symbol.kind() != SymbolKind.CONSTANT) {
                return false;
            }

            TypeSymbol constExprType = ((ConstantSymbol) symbol).broaderTypeDescriptor();
            return constExprType != null && constExprType.typeKind().isIntegerType();
        };
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ArrayTypeDescriptorNode node) {
        /*
        Array type descriptor suggests the completions only if the cursor is within the brackets
        eg:
        (1) TypeName[<cursor>] ...
         */
        int cursor = context.getCursorPositionInTree();
        for (ArrayDimensionNode arrayDimension : node.dimensions()) {
            Token openBracket = arrayDimension.openBracket();
            Token closeBracket = arrayDimension.closeBracket();
            if (cursor <= openBracket.textRange().startOffset()) {
                break;
            }
            if (cursor < closeBracket.textRange().endOffset()) {
                return true;
            }
        }
        
        return false;
    }
}
