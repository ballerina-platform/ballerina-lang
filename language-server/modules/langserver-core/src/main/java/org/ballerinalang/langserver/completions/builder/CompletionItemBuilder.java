package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.completions.util.SnippetBlock;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * Represents the base completion item builder.
 * 
 * @since 2.0.0
 */
public class CompletionItemBuilder {
    
    protected CompletionItemBuilder() {}
    /**
     * Get the {@link CompletionItemKind} for the symbol.
     * 
     * @param symbol associated symbol
     * @return {@link CompletionItemKind}
     */
    protected final CompletionItemKind getKind(Symbol symbol) {
        return switch (symbol.kind()) {
            case ENUM -> CompletionItemKind.Enum;
            default -> CompletionItemKind.Unit;
        };
    }

    /**
     * Get the snippet block's kind.
     * 
     * @param snippetBlock associated snippet block
     * @return {@link CompletionItemKind}
     */
    protected final CompletionItemKind getKind(SnippetBlock snippetBlock) {
        return switch (snippetBlock.kind()) {
            case KEYWORD -> CompletionItemKind.Keyword;
            case TYPE -> CompletionItemKind.TypeParameter;
            case VALUE -> CompletionItemKind.Value;
            default -> CompletionItemKind.Snippet;
        };
    }
}
