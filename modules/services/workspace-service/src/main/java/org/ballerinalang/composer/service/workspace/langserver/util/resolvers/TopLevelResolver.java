package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleConstantDefinitionContextResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContextResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleTypeNameContextResolver;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.util.parser.BallerinaParser;

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

        boolean noAt = findPreviousToken(dataModel, "@", 5) < 0;
        if (noAt && (errorContextResolver == null ||
                errorContextResolver == this ||
                parserRuleContext instanceof BallerinaParser.AnnotationAttachmentContext)) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
        }
        if (errorContextResolver instanceof PackageNameContextResolver) {
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleConstantDefinitionContextResolver) {
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleTypeNameContextResolver) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else {
            completionItems.addAll(
                    resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols, resolvers));
        }
        return completionItems;
    }

    void addStaticItem(List<CompletionItem> completionItems, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        item.setSortText(ItemResolverConstants.PRIORITY_7);
        completionItems.add(item);
    }

    /**
     * Add top level items to the given completionItems List
     *
     * @param completionItems - completionItems List
     * @param resolvers       - resolvers
     * @param dataModel       - datamodel
     * @param symbols         - all symbols upto this point
     */
    private void addTopLevelItems(ArrayList<CompletionItem> completionItems, HashMap<Class,
            AbstractItemResolver> resolvers, SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        addStaticItem(completionItems, ItemResolverConstants.IMPORT, ItemResolverConstants.IMPORT + " ");
        addStaticItem(completionItems, ItemResolverConstants.PACKAGE, ItemResolverConstants.PACKAGE + " ");
        addStaticItem(completionItems, ItemResolverConstants.CONST, ItemResolverConstants.CONST + " ");
        addStaticItem(completionItems, ItemResolverConstants.FUNCTION, ItemResolverConstants.FUNCTION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.SERVICE, ItemResolverConstants.SERVICE_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.CONNECTOR,
                ItemResolverConstants.CONNECTOR_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.STRUCT,
                ItemResolverConstants.STRUCT_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.ANNOTATION,
                ItemResolverConstants.ANNOTATION_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.XMLNS,
                ItemResolverConstants.NAMESPACE_DECLARATION_TEMPLATE);

        completionItems.addAll(
                resolvers.get(GlobalScope.class).resolveItems(dataModel, symbols, resolvers));
    }
}
