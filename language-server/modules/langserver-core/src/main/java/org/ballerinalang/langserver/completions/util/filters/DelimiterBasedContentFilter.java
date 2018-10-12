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
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the actions, functions and types in a package.
 */
public class DelimiterBasedContentFilter extends AbstractSymbolFilter {

    private static final Logger logger = LoggerFactory.getLogger(DelimiterBasedContentFilter.class);

    @Override
    public Either<List<CompletionItem>, List<SymbolInfo>> filterItems(LSServiceOperationContext completionContext) {

        List<String> poppedTokens = CommonUtil
                .popNFromStack(completionContext.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY), 3).stream()
                .map(Token::getText)
                .collect(Collectors.toList());

        String delimiter = "";
        for (String poppedToken : poppedTokens) {
            if (poppedToken.equals(UtilSymbolKeys.DOT_SYMBOL_KEY)
                    || poppedToken.equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)
                    || poppedToken.equals(UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY)
                    || poppedToken.equals(UtilSymbolKeys.BANG_SYMBOL_KEY)) {
                delimiter = poppedToken;
                break;
            }
        }
        
        ArrayList<SymbolInfo> returnSymbolsInfoList = new ArrayList<>();
        String tokenBeforeDelimiter = poppedTokens.get(poppedTokens.lastIndexOf(delimiter) - 1);

        if (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(delimiter)
                || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(delimiter)
                || UtilSymbolKeys.BANG_SYMBOL_KEY.equals(delimiter)) {
            returnSymbolsInfoList.addAll(FilterUtils.getInvocationAndFieldSymbolsOnVar(completionContext,
                    tokenBeforeDelimiter,
                    delimiter,
                    completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else if (UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(delimiter)) {
            // We are filtering the package functions, actions and the types
            Either<List<CompletionItem>, List<SymbolInfo>> filteredList = 
                    this.getActionsFunctionsAndTypes(completionContext, tokenBeforeDelimiter, delimiter);
            if (filteredList.isLeft()) {
                return Either.forLeft(filteredList.getLeft());
            }
            returnSymbolsInfoList.addAll(filteredList.getRight());
        }

        return Either.forRight(returnSymbolsInfoList);
    }

    /**
     * Get the actions, functions and types.
     * @param context               Text Document Service context (Completion Context)
     * @param pkgName               Package name to evaluate against
     * @param delimiter             Delimiter
     * @return {@link ArrayList}    List of filtered symbol info
     */
    private Either<List<CompletionItem>, List<SymbolInfo>> getActionsFunctionsAndTypes(
            LSServiceOperationContext context, String pkgName, String delimiter) {

        List<SymbolInfo> symbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);

        // Extract the package symbol
        SymbolInfo packageSymbolInfo = symbols.stream()
                .filter(item -> item.getSymbolName().equals(pkgName)
                        && item.getScopeEntry().symbol instanceof BPackageSymbol)
                .findFirst()
                .orElse(null);

        if (packageSymbolInfo != null) {
            Scope.ScopeEntry packageEntry = packageSymbolInfo.getScopeEntry();
            PackageID packageID = packageEntry.symbol.pkgID;
            LSIndexImpl lsIndex = context.get(LSGlobalContextKeys.LS_INDEX_KEY);
            BPackageSymbolDAO pkgSymbolDAO = ((BPackageSymbolDAO) lsIndex.getDaoFactory().get(DAOType.PACKAGE_SYMBOL));
            BPackageSymbolDTO pkgDTO = new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                    .setName(packageID.getName().getValue())
                    .setOrgName(packageID.getOrgName().getValue())
                    .build();

            try {
                List<BFunctionSymbolDTO> funcDTOs = pkgSymbolDAO.getFunctions(pkgDTO, -1, false, false);
                List<BRecordTypeSymbolDTO> recordDTOs = pkgSymbolDAO.getRecords(pkgDTO, false);
                List<OtherTypeSymbolDTO> otherTypeDTOs = pkgSymbolDAO.getOtherTypes(pkgDTO);
                List<BObjectTypeSymbolDTO> objDTOs = pkgSymbolDAO.getObjects(pkgDTO, false);
                
                if (funcDTOs.isEmpty() && recordDTOs.isEmpty() && otherTypeDTOs.isEmpty() && objDTOs.isEmpty()) {
                    return this.filterSymbolsOnFallback(context, pkgName, delimiter);
                }
                List<CompletionItem> completionItems = funcDTOs.stream()
                        .map(BFunctionSymbolDTO::getCompletionItem)
                        .collect(Collectors.toList());
                completionItems.addAll(recordDTOs.stream()
                                .map(BRecordTypeSymbolDTO::getCompletionItem)
                                .collect(Collectors.toList()));
                completionItems.addAll(otherTypeDTOs.stream()
                                .map(OtherTypeSymbolDTO::getCompletionItem)
                                .collect(Collectors.toList()));
                completionItems.addAll(objDTOs.stream()
                                .map(BObjectTypeSymbolDTO::getCompletionItem)
                                .collect(Collectors.toList()));
                return Either.forLeft(completionItems);
            } catch (LSIndexException e) {
                logger.warn("Error retrieving Completion Items from Index DB.");
                return this.filterSymbolsOnFallback(context, pkgName, delimiter);
            }
        }

        return Either.forRight(new ArrayList<>());
    }
    
    private Either<List<CompletionItem>, List<SymbolInfo>> filterSymbolsOnFallback(LSServiceOperationContext context,
                                                                                   String pkgName, String delimiter) {
        List<SymbolInfo> visibleSymbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> filteredSymbols = FilterUtils.getInvocationAndFieldSymbolsOnVar(context, pkgName,
                delimiter, visibleSymbols);
        return Either.forRight(filteredSymbols);
    }
}
