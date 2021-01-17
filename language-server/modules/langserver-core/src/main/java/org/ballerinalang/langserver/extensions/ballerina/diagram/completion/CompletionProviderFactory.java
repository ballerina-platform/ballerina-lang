package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.extensions.ballerina.diagram.completion.spi.DiagramCompletionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class CompletionProviderFactory {
    private static final Map<Class<?>, DiagramCompletionProvider<Node>> providers = new HashMap<>();

    private static final CompletionProviderFactory INSTANCE = new CompletionProviderFactory();

    private CompletionProviderFactory() {
        ServiceLoader<DiagramCompletionProvider> providerServices = ServiceLoader.load(DiagramCompletionProvider.class);
        for (DiagramCompletionProvider<Node> provider : providerServices) {
            if (provider == null) {
                continue;
            }
            for (Class<?> attachmentPoint : provider.getAttachmentPoints()) {
                if (!providers.containsKey(attachmentPoint)) {
                    providers.put(attachmentPoint, provider);
                }
            }
        }
    }

    public static CompletionProviderFactory instance() {
        return INSTANCE;
    }
}
