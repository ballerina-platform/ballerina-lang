package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.AnnotationAttachment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ServiceContextResolver
 */
public class ServiceContextResolver extends AbstractItemResolver {

    @Override
    ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                           HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        // Add resource
        CompletionItem resource = new CompletionItem();
        resource.setLabel(ItemResolverConstants.RESOURCE_TYPE);
        resource.setInsertText("resource ${1:name} (message ${2:m}){\n    ${3}\n}");
        resource.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        resource.setSortText(ItemResolverConstants.PRIORITY_7);
        completionItems.add(resource);

        // Add annotations
        completionItems.addAll(resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols , resolvers));

        return completionItems;
    }
}
