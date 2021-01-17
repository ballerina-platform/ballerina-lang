package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.spi.DiagramCompletionProvider;

import java.util.List;

public abstract class AbstractDiagramCompletionProvider<T extends Node> implements DiagramCompletionProvider {
    private final List<Class<T>> attachmentPoints;

    public AbstractDiagramCompletionProvider(List<Class<T>> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }

    @Override
    public boolean onPreValidation(DiagramCompletionContext context, Node node) {
        return true;
    }

    @Override
    public List<Class<T>> getAttachmentPoints() {
        return this.attachmentPoints;
    }
}
