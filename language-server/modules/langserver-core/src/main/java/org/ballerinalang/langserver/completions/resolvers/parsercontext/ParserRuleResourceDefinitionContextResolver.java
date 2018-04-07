package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Completion Item resolver for the resource definition context.
 */
public class ParserRuleResourceDefinitionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        CompletionItem resource = new CompletionItem();
        resource.setLabel(ItemResolverConstants.RESOURCE_TYPE);
        resource.setInsertText(Snippet.RESOURCE.toString());
        resource.setInsertTextFormat(InsertTextFormat.Snippet);
        resource.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        
        return new ArrayList<>(Collections.singletonList(resource));
    }
}
