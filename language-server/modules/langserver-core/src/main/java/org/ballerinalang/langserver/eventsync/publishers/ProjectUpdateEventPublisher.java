/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.eventsync.publishers;

import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;
import org.ballerinalang.langserver.eventsync.EventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Publishes the project update event.
 *
 * @since 2201.1.0
 */
public class ProjectUpdateEventPublisher implements EventPublisher {
    public static final String NAME = "Project Update Event";
    public static List<EventSubscriber> publisherSubscribersList = new ArrayList();
    private CompletableFuture<Boolean> latestScheduled = null;
    private static final long DIAGNOSTIC_DELAY = 1;

    @Override
    public List<EventSubscriber> getSubscribers() {
        return publisherSubscribersList;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void subscribe(EventSubscriber subscriber) {
        if (!publisherSubscribersList.contains(subscriber)) {
            publisherSubscribersList.add(subscriber);
        }
    }

    // ToDo: whether we need unsubscribe
    @Override
    public void unsubscribe(EventSubscriber subscriber) {

    }

    @Override
    public void publish(ExtendedLanguageClient client, LanguageServerContext serverContext,
                        DocumentServiceContext context) {
        if (latestScheduled != null && !latestScheduled.isDone()) {
            latestScheduled.completeExceptionally(new Throwable("Cancelled project update event publisher"));
        }

        Executor delayedExecutor = CompletableFuture.delayedExecutor(DIAGNOSTIC_DELAY, TimeUnit.SECONDS);
        CompletableFuture<Boolean> scheduledFuture = CompletableFuture.supplyAsync(() -> true, delayedExecutor);
        latestScheduled = scheduledFuture;
        
        publisherSubscribersList.
                forEach(subscriber -> subscriber.announce(client, context, serverContext, scheduledFuture));
    }
}
