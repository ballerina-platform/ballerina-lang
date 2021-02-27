package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.SnippetBlock;
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
        switch (symbol.kind()) {
            case ENUM:
                return CompletionItemKind.Enum;
            default:
                return CompletionItemKind.Unit;
        }
    }

    /**
     * Get the snippet block's kind.
     * 
     * @param snippetBlock associated snippet block
     * @return {@link CompletionItemKind}
     */
    protected final CompletionItemKind getKind(SnippetBlock snippetBlock) {
        switch (snippetBlock.kind()) {
            case KEYWORD:
                return CompletionItemKind.Keyword;
            case TYPE:
                return CompletionItemKind.TypeParameter;
            case SNIPPET:
            case STATEMENT:
            default:
                return CompletionItemKind.Snippet;
        }
    }
}
