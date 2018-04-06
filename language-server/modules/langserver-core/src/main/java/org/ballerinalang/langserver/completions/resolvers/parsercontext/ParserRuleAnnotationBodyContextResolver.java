/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;

/**
 * annotation body context resolver for the completion items.
 */
public class ParserRuleAnnotationBodyContextResolver extends AbstractItemResolver {

    /**
     * here we provide the attach keyword completion item.
     * @param completionContext completion operation context
     * @return {@link ArrayList}
     */
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int tokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        int searchIndex = tokenIndex - 1;

        while (true) {
            if (searchIndex < 0) {
                break;
            }
            int channel = tokenStream.get(searchIndex).getChannel();
            String tokenStr = tokenStream.get(searchIndex).getText();
            if (channel == 0 && (tokenStr.equals(",") || tokenStr.equals(ItemResolverConstants.ATTACH))) {
                return completionItems;
            } else if (channel == 0) {
                break;
            }
            searchIndex--;
        }

        CompletionItem attachCompletionItem = new CompletionItem();
        attachCompletionItem.setInsertText(ItemResolverConstants.ATTACH + " ");
        attachCompletionItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        attachCompletionItem.setLabel(ItemResolverConstants.ATTACH);
        completionItems.add(attachCompletionItem);
        return completionItems;
    }
}
