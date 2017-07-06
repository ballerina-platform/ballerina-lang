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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.util.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Completion Item Resolver for the Package name context
 */
class PackageNameContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ArrayList<SymbolInfo> searchList = symbols;

        TokenStream tokenStream = dataModel.getTokenStream();
        int currentTokenIndex = dataModel.getTokenIndex();
        int tokenIterator = currentTokenIndex - 1;
        boolean proceed = true;

        while (proceed) {
            String tokenStr = tokenStream.get(tokenIterator).getText();
            if (tokenStr.equals(ItemResolverConstants.IMPORT) || tokenStr.equals(ItemResolverConstants.PACKAGE)) {
                proceed = false;
            } else {
                tokenIterator--;
            }
        }

        String tokenString = tokenStream.get(tokenIterator).getText();

        StringBuilder packageBuilder = new StringBuilder();
        for (int i = tokenIterator + 1; i < currentTokenIndex; i++) {
            Token token = tokenStream.get(i);
            int type = token.getType();
            if (type == BallerinaParser.WS || type == BallerinaParser.NEW_LINE) {
                continue;
            }
            packageBuilder.append(token.getText());
        }
        String packagePartial = packageBuilder.toString();
        int lastDot = packagePartial.lastIndexOf('.');
        if (lastDot >= 0) {
            packagePartial = packagePartial.substring(0, lastDot + 1);
        } else {
            packagePartial = "";
        }
        String packageTillDot = packagePartial;
        int from = packageTillDot.length();

        if (tokenString.equals(ItemResolverConstants.IMPORT)) {
            List<SymbolInfo> filteredSymbolInfoList = searchList.stream()
                    .filter(
                            symbolInfo -> (symbolInfo.getSymbol() instanceof NativePackageProxy)
                    ).collect(Collectors.toList());

            List<String> packagePrefix = filteredSymbolInfoList.stream()
                    .filter(s -> s.getSymbolName()
                    .startsWith(packageTillDot))
                    .map(s -> firstPart(s, from)).distinct()
                    .collect(Collectors.toList());

            packagePrefix.forEach((symbolInfo -> {
                // For each token call the api to get the items related to the token
                CompletionItem completionItem = new CompletionItem();
                completionItem.setLabel(symbolInfo.substring(0, symbolInfo.length() - 1));
                completionItem.setInsertText(symbolInfo);
                completionItem.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                completionItem.setSortText(ItemResolverConstants.PRIORITY_4);
                completionItems.add(completionItem);
            }));
        }

        return completionItems;
    }

    private String firstPart(SymbolInfo s, int from) {
        String symbolName = s.getSymbolName();
        int dot = symbolName.indexOf('.', from);
        if (dot >= 0) {
            return symbolName.substring(from, dot) + '.';
        } else {
            return symbolName.substring(from) + ';';
        }

    }
}
