package org.ballerinalang.langserver.eventsync.subscribers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

/**
 * Updates the package map in LSPackage loader.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class PullModuleSubscriber implements EventSubscriber {

    public static final String NAME = "Pull module subscriber";

    @Override
    public EventKind eventKind() {
        return EventKind.PULL_MODULE;
    }

    @Override
    public void onEvent(ExtendedLanguageClient client, DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
        LSPackageLoader.getInstance(languageServerContext).updatePackageMapOnPullModuleEvent(context);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
