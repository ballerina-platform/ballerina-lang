package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;

public class TopLevelResolver implements ItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        CompletionItem importItem = new CompletionItem();
        importItem.setLabel(ItemResolverConstants.IMPORT);
        importItem.setInsertText(ItemResolverConstants.IMPORT + " ");
        importItem.setDetail(ItemResolverConstants.KEYWORD);
        importItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(importItem);

        CompletionItem packageItem = new CompletionItem();
        packageItem.setLabel(ItemResolverConstants.PACKAGE);
        packageItem.setInsertText(ItemResolverConstants.PACKAGE + " ");
        packageItem.setDetail(ItemResolverConstants.KEYWORD);
        packageItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(packageItem);

        CompletionItem functionItem = new CompletionItem();
        functionItem.setLabel("function");
        functionItem.setInsertText("function ${1:name} (${2}) { \n    ${3}\n}");
        functionItem.setDetail(ItemResolverConstants.KEYWORD);
        functionItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(functionItem);
        return completionItems;
    }
}
