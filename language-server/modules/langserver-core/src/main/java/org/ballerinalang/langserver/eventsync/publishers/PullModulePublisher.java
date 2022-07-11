package org.ballerinalang.langserver.eventsync.publishers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.eventsync.AbstractEventPublisher;

/**
 * Publishes the pull module event.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.eventsync.EventPublisher")
public class PullModulePublisher extends AbstractEventPublisher {

    public static final String NAME = "Pull module event publisher";

    @Override
    public EventKind getKind() {
        return EventKind.PULL_MODULE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void publish(ExtendedLanguageClient client, LanguageServerContext serverContext,
                        DocumentServiceContext context) {
        subscribers.forEach(subscriber -> subscriber.onEvent(client, context, serverContext));
    }
}
