package org.ballerinalang.langserver.contexts;

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.Token;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.completions.TomlCompletionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Language server Toml context implementation.
 *
 * @since 2.0.0
 */
public class TomlCompletionContextImpl extends CompletionContextImpl implements TomlCompletionContext {

    private final List<Node> resolverChain = new ArrayList<>();
    private Token tokenAtCursor;
    private NonTerminalNode nodeAtCursor;

    public TomlCompletionContextImpl(CompletionContext context) {
        super(context.operation(),
                context.fileUri(),
                context.workspace(),
                context.getCapabilities(),
                context.getCursorPosition());
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode node) {
        if (this.nodeAtCursor != null) {
            throw new RuntimeException("Setting the node more than once is not allowed");
        }
        this.nodeAtCursor = node;
    }

    @Override
    public NonTerminalNode getNodeAtCursor() {
        return this.nodeAtCursor;
    }

    @Override
    public void addResolver(Node node) {
        this.resolverChain.add(node);
    }

    @Override
    public List<Node> getResolverChain() {
        return this.resolverChain;
    }
}
