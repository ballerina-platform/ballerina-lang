package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.AnnotationAttachment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        CompletionItem importItem = new CompletionItem();
        importItem.setLabel(ItemResolverConstants.IMPORT);
        importItem.setInsertText(ItemResolverConstants.IMPORT + " ");
        importItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        importItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(importItem);

        CompletionItem packageItem = new CompletionItem();
        packageItem.setLabel(ItemResolverConstants.PACKAGE);
        packageItem.setInsertText(ItemResolverConstants.PACKAGE + " ");
        packageItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        packageItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(packageItem);

        CompletionItem functionItem = new CompletionItem();
        functionItem.setLabel(ItemResolverConstants.FUNCTION);
        functionItem.setInsertText(ItemResolverConstants.FUNCTION_TEMPLATE);
        functionItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        functionItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(functionItem);

        CompletionItem serviceItem = new CompletionItem();
        serviceItem.setLabel(ItemResolverConstants.SERVICE);
        serviceItem.setInsertText(ItemResolverConstants.SERVICE_TEMPLATE);
        serviceItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        serviceItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(serviceItem);

        CompletionItem connectorDefItem = new CompletionItem();
        connectorDefItem.setLabel(ItemResolverConstants.CONNECTOR);
        connectorDefItem.setInsertText(ItemResolverConstants.CONNECTOR_DEFFINITION_TEMPLATE);
        connectorDefItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        connectorDefItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(connectorDefItem);

        completionItems.addAll(resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols, resolvers));
        return completionItems;
    }
}
