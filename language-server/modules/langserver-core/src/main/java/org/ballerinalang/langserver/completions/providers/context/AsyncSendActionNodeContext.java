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

import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.List;

/**
 * Handles the completions for the {@link AsyncSendActionNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class AsyncSendActionNodeContext extends RightArrowActionNodeContext<AsyncSendActionNode> {

    public AsyncSendActionNodeContext() {
        super(AsyncSendActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, AsyncSendActionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = this.getFilteredItems(context, node, node.expression());
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, AsyncSendActionNode node, 
                     List<LSCompletionItem> completionItems) {
        for (int i = 0; i < completionItems.size(); i++) {
            LSCompletionItem completionItem = completionItems.get(i);
            sortByAssignability(context, completionItem, SortingUtil.toRank(context, completionItem, i + 1));
        }
    }
}
