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
package org.ballerinalang.langserver.completions.util.filters;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.SymbolInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the actions and the functions in a package.
 */
public class PackageActionFunctionAndTypesFilter extends SymbolFilter {

    private static final String CREATE_KEYWORD = "create";

    private static final String PKG_DELIMITER_KEYWORD = ":";

    private static final String DOT_SYMBOL_KEY = ".";

    private static final String CONNECTOR_KIND = "CONNECTOR";

    @Override
    public List<SymbolInfo> filterItems(TextDocumentServiceContext completionContext) {

        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int delimiterIndex = this.getPackageDelimiterTokenIndex(completionContext);
        String delimiter = tokenStream.get(delimiterIndex).getText();
        ArrayList<SymbolInfo> returnSymbolsInfoList = new ArrayList<>();

        if (DOT_SYMBOL_KEY.equals(delimiter)) {
            // If the delimiter is "." then we are filtering the bound functions for the structs
            returnSymbolsInfoList.addAll(this.invocationsAndFieldsOnIdentifier(completionContext, delimiterIndex));
        } else if (PKG_DELIMITER_KEYWORD.equals(delimiter)) {
            // We are filtering the package functions, actions and the types
            ArrayList<SymbolInfo> filteredList = this.getActionsFunctionsAndTypes(completionContext, delimiterIndex);

            // If this is a connector init
            if (isConnectorInit(delimiterIndex, completionContext)) {
                List<SymbolInfo> connectorKindList = filteredList
                        .stream()
                        .filter(item -> item.getScopeEntry().symbol.kind.toString().equals(CONNECTOR_KIND)).
                                collect(Collectors.toList());
                returnSymbolsInfoList.addAll(connectorKindList);
            } else {
                returnSymbolsInfoList.addAll(filteredList);
            }
        }

        return returnSymbolsInfoList;
    }

    /**
     * Check whether the statement being writing is a connector init by analyzing the tokens.
     * @param startIndex    Search start index
     * @param context       Document service context
     * @return {@link Boolean} connector init or not
     */
    private boolean isConnectorInit(int startIndex, TextDocumentServiceContext context) {
        int nonHiddenTokenCount = 0;
        int counter = startIndex - 1;
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);

        while (true) {
            Token token = tokenStream.get(counter);
            if (nonHiddenTokenCount == 2 && tokenStream.get(counter + 1).getText().equals(CREATE_KEYWORD)) {
                return true;
            } else if (nonHiddenTokenCount == 2) {
                break;
            }

            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                nonHiddenTokenCount++;
            }
            counter--;
        }

        return false;
    }
}
