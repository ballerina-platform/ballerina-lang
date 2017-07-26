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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.AbstractItemResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.util.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * annotation body context resolver for the completion items
 */
public class ParserRuleAnnotationBodyContextResolver extends AbstractItemResolver {

    /**
     * here we provide the attach keyword completion item
     * @param dataModel suggestions filter data model
     * @param symbols symbols list
     * @param resolvers item resolvers map
     * @return {@link ArrayList}
     */
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = dataModel.getTokenStream();
        int tokenIndex = dataModel.getTokenIndex();
        int searchIndex = tokenIndex - 1;

        while (true) {
            if (searchIndex < 0) {
                break;
            }
            int channel = tokenStream.get(searchIndex).getChannel();
            String tokenStr = tokenStream.get(searchIndex).getText();
            if (channel == 0 && (tokenStr.equals(",") || tokenStr.equals(ItemResolverConstants.ATTACH))) {
                completionItems.addAll(resolvers.get(BallerinaParser.AttachmentPointContext.class)
                        .resolveItems(dataModel, symbols, resolvers));
                return completionItems;
            } else if (channel == 0) {
                break;
            }
            searchIndex--;
        }

        CompletionItem attachCompletionItem = new CompletionItem();
        attachCompletionItem.setInsertText(ItemResolverConstants.ATTACH + " ");
        attachCompletionItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        attachCompletionItem.setSortText(ItemResolverConstants.PRIORITY_7);
        attachCompletionItem.setLabel(ItemResolverConstants.ATTACH);
        completionItems.add(attachCompletionItem);
        return completionItems;
    }
}
