package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.OnFailCheckNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link OnFailCheckNode} context.
 *
 * @since 2201.8.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OnFailCheckNodeContext extends AbstractCompletionProvider<OnFailCheckNode> {
    public OnFailCheckNodeContext() {
        super(OnFailCheckNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, OnFailCheckNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        int cursor = context.getCursorPositionInTree();

        if (this.onErrorConstructor(cursor, node)) {
            /*
            Covers the following
            (1) on fail e => <cursor>
            */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ERROR.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        } else if (this.cursorAfterIdentifier(cursor, node)) {
            /*
            Covers the following
            (1) on fail e <cursor>
            (2) on fail e = <cursor>
            */
            return Collections.emptyList();
        }

        this.sort(context, node, completionItems);
        return completionItems;
    }

    private boolean cursorAfterIdentifier(int cursor, OnFailCheckNode node) {
        return cursor > node.identifier().textRange().endOffset();
    }

    private boolean onErrorConstructor(int cursor, OnFailCheckNode node) {
        return cursor >= node.rightArrowToken().textRange().endOffset();
    }
}
