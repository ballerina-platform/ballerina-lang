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

import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;

import java.util.List;

/**
 * Completion provider for {@link TypedBindingPatternNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TypedBindingPatternNodeContext extends AbstractCompletionProvider<TypedBindingPatternNode> {

    public TypedBindingPatternNodeContext() {
        super(TypedBindingPatternNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TypedBindingPatternNode node)
            throws LSCompletionException {
        /*
        When comes to the typed binding pattern we route to the type descriptors to check whether there are resolvers
        associated with the type descriptor. Otherwise the router will go up the parent ladder.
         */
        return CompletionUtil.route(context, node.typeDescriptor());
    }
}
