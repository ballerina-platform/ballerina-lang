package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.AnnotationAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();


        ParserRuleContext parserRuleContext = dataModel.getParserRuleContext();
        AbstractItemResolver errorContextResolver = parserRuleContext == null ? null :
                resolvers.get(parserRuleContext.getClass());
        if (errorContextResolver != null && errorContextResolver != this) {
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else {
            if (!this.isAnnotationContext(dataModel)) {
                addStaticItem(completionItems, ItemResolverConstants.IMPORT, ItemResolverConstants.IMPORT + " ");
                addStaticItem(completionItems, ItemResolverConstants.PACKAGE, ItemResolverConstants.PACKAGE + " ");
                addStaticItem(completionItems, ItemResolverConstants.FUNCTION, ItemResolverConstants.FUNCTION_TEMPLATE);
                addStaticItem(completionItems, ItemResolverConstants.SERVICE, ItemResolverConstants.SERVICE_TEMPLATE);
                addStaticItem(completionItems, ItemResolverConstants.CONNECTOR,
                        ItemResolverConstants.CONNECTOR_DEFFINITION_TEMPLATE);
                addStaticItem(completionItems, ItemResolverConstants.STRUCT,
                        ItemResolverConstants.STRUCT_DEFFINITION_TEMPLATE);
                addStaticItem(completionItems, ItemResolverConstants.ANNOTATION,
                        ItemResolverConstants.ANNOTATION_DEFFINITION_TEMPLATE);

            }
            completionItems.addAll(resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols, resolvers));
        }
        return completionItems;
    }

    void addStaticItem(List<CompletionItem> completionItems, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        item.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(item);
    }
}
