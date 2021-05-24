package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;

import javax.annotation.Nullable;

/**
 * Represents a FunctionPointer symbol based completion item.
 *
 * @since 2.0.0
 */
public class FunctionPointerCompletionItem extends SymbolCompletionItem {

    public FunctionPointerCompletionItem(BallerinaCompletionContext lsContext, @Nullable Symbol bSymbol,
                                         CompletionItem completionItem) {
        super(lsContext, bSymbol, completionItem, CompletionItemType.FUNCTION_POINTER);
    }
}
