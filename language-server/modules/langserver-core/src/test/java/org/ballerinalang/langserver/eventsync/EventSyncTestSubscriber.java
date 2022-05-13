package org.ballerinalang.langserver.eventsync;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.PublisherKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

import java.util.Collections;
import java.util.List;

/**
 * Test Subscriber to test Event Sync publish subscriber.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class EventSyncTestSubscriber implements EventSubscriber {

    public static final String NAME = "Event Sync Test Subscriber";
    public static boolean gotEvent = false;

    @Override
    public List<PublisherKind> publisherKinds() {
        return Collections.singletonList(PublisherKind.PROJECT_UPDATE_EVENT_PUBLISHER);
    }

    @Override
    public void onEvent(ExtendedLanguageClient client,
                        DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
        gotEvent = true;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
