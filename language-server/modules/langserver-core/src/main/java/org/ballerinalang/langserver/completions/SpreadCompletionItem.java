package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Optional;

/**
 * Represents a Spread Completion Item.
 * Eg: ...varName
 *
 * @since 2201.8.0
 */
public class SpreadCompletionItem extends AbstractLSCompletionItem {
    private final Symbol expression;

    public SpreadCompletionItem(BallerinaCompletionContext context, CompletionItem completionItem, Symbol expression) {
        super(context, completionItem, CompletionItemType.SPREAD);
        this.expression = expression;
    }

    public Optional<Symbol> getExpression() {
        return Optional.ofNullable(expression);
    }
}
