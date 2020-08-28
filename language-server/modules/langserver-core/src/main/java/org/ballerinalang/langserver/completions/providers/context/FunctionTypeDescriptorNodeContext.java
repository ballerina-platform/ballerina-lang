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

import io.ballerina.tools.text.TextRange;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.ReturnTypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link FunctionTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FunctionTypeDescriptorNodeContext extends AbstractCompletionProvider<FunctionTypeDescriptorNode> {

    public FunctionTypeDescriptorNodeContext() {
        super(FunctionTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FunctionTypeDescriptorNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (this.withinParameterContext(context, node)) {
            /*
            Covers the completions when the cursor is within the parameter context
             */
            if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                List<Scope.ScopeEntry> typesInModule = QNameReferenceUtil.getTypesInModule(context,
                        ((QualifiedNameReferenceNode) nodeAtCursor));
                return this.getCompletionItemList(typesInModule, context);
            }

            List<LSCompletionItem> completionItems = this.getPackagesCompletionItems(context);
            completionItems.addAll(this.getTypeItems(context));

            return completionItems;
        }

        if (this.withinReturnKWContext(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
        }

        return new ArrayList<>();
    }

    private boolean withinParameterContext(LSContext context, FunctionTypeDescriptorNode node) {
        FunctionSignatureNode functionSignatureNode = node.functionSignature();
        if (functionSignatureNode.isMissing()) {
            return false;
        }
        Integer txtPosInTree = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TextRange openParanRange = functionSignatureNode.openParenToken().textRange();
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();

        return openParanRange.endOffset() <= txtPosInTree && txtPosInTree <= closeParanRange.startOffset();
    }

    private boolean withinReturnKWContext(LSContext context, FunctionTypeDescriptorNode node) {
        FunctionSignatureNode functionSignatureNode = node.functionSignature();
        if (functionSignatureNode.isMissing()) {
            return false;
        }
        Integer txtPosInTree = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();
        Optional<ReturnTypeDescriptorNode> returnTypeDescNode = functionSignatureNode.returnTypeDesc();

        return closeParanRange.startOffset() <= txtPosInTree && (!returnTypeDescNode.isPresent()
                || returnTypeDescNode.get().returnsKeyword().isMissing());
    }
}
