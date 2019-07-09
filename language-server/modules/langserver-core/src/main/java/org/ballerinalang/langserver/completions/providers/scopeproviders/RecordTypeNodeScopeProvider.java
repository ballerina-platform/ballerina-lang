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
package org.ballerinalang.langserver.completions.providers.scopeproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item Resolver for the BLangRecord node context.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class RecordTypeNodeScopeProvider extends LSCompletionProvider {

    public RecordTypeNodeScopeProvider() {
        this.attachmentPoints.add(BLangRecordTypeNode.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationOrDelimiterTokenType > -1) {
            Either<List<CompletionItem>, List<SymbolInfo>> eitherList = SymbolFilters
                    .get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(eitherList, context));
        } else {
            List<SymbolInfo> filteredTypes = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY).stream()
                    .filter(symbolInfo -> FilterUtils.isBTypeEntry(symbolInfo.getScopeEntry()))
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredTypes, context));
            completionItems.addAll(this.getPackagesCompletionItems(context));
        }
        return completionItems;
    }
}
