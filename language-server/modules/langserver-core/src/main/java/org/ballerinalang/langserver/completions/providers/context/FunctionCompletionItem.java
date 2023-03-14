/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.eclipse.lsp4j.CompletionItem;

/**
 * Completion item for main function.
 *
 * @since 2201.4.0
 */
public class FunctionCompletionItem extends AbstractLSCompletionItem {
    private final CompletionItem completionItem;
    public FunctionCompletionItem(BallerinaCompletionContext context, CompletionItem completionItem) {
        super(context, completionItem, CompletionItemType.SNIPPET);
        this.completionItem = completionItem;
    }

    @Override
    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
