package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.List;

/**
 * Interface for completion item providers.
 *
 * @param <T> generic syntax tree node.
 * @since 2201.6.0
 */
public interface CompletionProvider<T extends Node> {

    /**
     * Get the name of the completion provider.
     *
     * @return {@link String}   Name of the completion provider
     */
    String name();

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context completion operation Context
     * @param node    Node instance for the parser context
     * @return {@link List}     List of calculated Completion Items
     * @throws CompletionException when completion fails
     */
    List<CompletionItem> getCompletions(CompletionContext context, T node) throws CompletionException;

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    List<Class<T>> getAttachmentPoints();
}
