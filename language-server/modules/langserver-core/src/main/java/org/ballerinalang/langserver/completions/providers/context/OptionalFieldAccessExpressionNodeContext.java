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

import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.List;

/**
 * Completion provider for {@link OptionalFieldAccessExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OptionalFieldAccessExpressionNodeContext
        extends AbstractFieldAccessExpressionNodeContext<OptionalFieldAccessExpressionNode> {
    public OptionalFieldAccessExpressionNodeContext() {
        super(OptionalFieldAccessExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, OptionalFieldAccessExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = getEntries(ctx, node.expression());
        this.sort(ctx, node, completionItems);

        return completionItems;
    }
}
