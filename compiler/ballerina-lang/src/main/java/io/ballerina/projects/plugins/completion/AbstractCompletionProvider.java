package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.List;

/**
 * Interface for completion item providers.
 *
 * @param <T> Provider's node type
 * @since 2201.6.0
 */
public abstract class AbstractCompletionProvider<T extends Node> implements CompletionProvider<T> {

    private final List<Class<T>> attachmentPoints;

    public AbstractCompletionProvider(List<Class<T>> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }

    public AbstractCompletionProvider(Class<T> attachmentPoint) {
        this.attachmentPoints = List.of(attachmentPoint);
    }

    @Override
    public List<Class<T>> getAttachmentPoints() {
        return this.attachmentPoints;
    }
}
