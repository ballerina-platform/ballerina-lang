package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PackageActionsAndFunctionsResolver
 */
public class PackageActionsAndFunctionsResolver extends AbstractItemResolver {
    @Override
    ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                           HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        List<SymbolInfo> searchList = filterPackageActionsAndFunctions(dataModel, symbols);

        populateCompletionItemList(searchList, completionItems);

        return completionItems;
    }
}
