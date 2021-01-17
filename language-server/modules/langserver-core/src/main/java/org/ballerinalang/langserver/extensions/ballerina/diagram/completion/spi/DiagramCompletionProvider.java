package org.ballerinalang.langserver.extensions.ballerina.diagram.completion.spi;

import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionContext;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.DiagramCompletionItem;

import java.util.List;

public interface DiagramCompletionProvider<T extends Node> {
    List<DiagramCompletionItem> getCompletions(DiagramCompletionContext context, T node);

    boolean onPreValidation(DiagramCompletionContext context, T node);
    
    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    List<Class<T>> getAttachmentPoints();
}
