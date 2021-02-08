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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Completion provider for {@link RecordTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RecordTypeDescriptorNodeContext extends AbstractCompletionProvider<RecordTypeDescriptorNode> {

    public RecordTypeDescriptorNodeContext() {
        super(RecordTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, RecordTypeDescriptorNode node) {
        /*
        Covers the following cases,
        (1) public type T5 record {
                i<cursor>
            };
        (2) public type T5 record {
                <cursor>
            };
        (3) public type T5 record {
                mod:<cursor>
            };
        (4) public type T5 record {
                mod:a<cursor>
            };
         */
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            Predicate<Symbol> predicate =
                    symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> types = QNameReferenceUtil.getModuleContent(context,
                    (QualifiedNameReferenceNode) nodeAtCursor, predicate);
            completionItems.addAll(this.getCompletionItemList(types, context));
        } else {
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getTypeItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }
}
