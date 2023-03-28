package org.ballerinalang.langserver.completions;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.CompletionExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Collections;
import java.util.List;

public class CompilerPluginCompletionExtension implements CompletionExtension {

    @Override
    public boolean validate(CompletionParams inputParams) {
        return inputParams.getTextDocument().getUri().endsWith(".bal");
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams, CompletionContext context, 
                                        LanguageServerContext serverContext) throws Throwable {
        
        return Collections.emptyList();
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams, CompletionContext context, 
                                        LanguageServerContext serverContext, 
                                        CancelChecker cancelChecker) throws Throwable {
        return CompletionExtension.super.execute(inputParams, context, serverContext, cancelChecker);
    }

    @Override
    public List<String> handledCustomURISchemes(CompletionParams inputParams,
                                                CompletionContext context,
                                                LanguageServerContext serverContext) {
        return Collections.singletonList(CommonUtil.URI_SCHEME_EXPR);
    }
}
