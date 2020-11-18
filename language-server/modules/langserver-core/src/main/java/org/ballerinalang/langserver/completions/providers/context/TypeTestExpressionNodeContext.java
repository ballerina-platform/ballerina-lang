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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link TypeTestExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TypeTestExpressionNodeContext extends AbstractCompletionProvider<TypeTestExpressionNode> {

    public TypeTestExpressionNodeContext() {
        super(TypeTestExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TypeTestExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onQualifiedNameIdentifier(context, node.typeDescriptor())) {
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) node.typeDescriptor())));
            module.ifPresent(scopeEntry ->
                    completionItems.addAll(this.getCompletionItemList(this.filterTypesInModule(module.get()),
                            context)));
        } else {
            completionItems.addAll(this.getTypeItems(context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }

        return completionItems;
    }
}
