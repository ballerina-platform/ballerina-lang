package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.natives.NativePackageProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Package name context
 */
class PackageNameContextResolver implements ItemResolver {
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
        if (tokenString.equals(ItemResolverConstants.IMPORT)) {
            List<SymbolInfo> filteredSymbolInfoList = searchList.stream()
                    .filter(
                            symbolInfo -> (symbolInfo.getSymbol() instanceof NativePackageProxy)
                    ).collect(Collectors.toList());

            filteredSymbolInfoList.forEach((symbolInfo -> {
                // For each token call the api to get the items related to the token
                CompletionItem completionItem = new CompletionItem();
                completionItem.setLabel(symbolInfo.getSymbolName());
                completionItem.setInsertText(symbolInfo.getSymbolName());
                completionItems.add(completionItem);
            }));
        }

        return completionItems;
    }
}
