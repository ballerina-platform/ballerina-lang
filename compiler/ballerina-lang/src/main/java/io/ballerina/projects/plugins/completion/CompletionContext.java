package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.codeaction.PositionedActionContext;

/**
 * Code action context.
 *
 * @since 2201.6.0
 */
public interface CompletionContext extends PositionedActionContext {
    
    /**
     * Returns the node at the current cursor position.
     *
     * @return {@link CompletionItem}
     */
    Node nodeAtCursor();
    
    /**
     * Returns the cursor position in the syntax tree.
     *
     * @return {@link CompletionItem}
     */
    int cursorPosInTree();

}
