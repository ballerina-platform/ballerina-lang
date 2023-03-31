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
package org.ballerinalang.langserver.eventsync;

import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Subscribes and provide the Event Subscribers.
 *
 * @since 2201.1.1
 */
public class EventSyncPubSubHolder {
    private static final LanguageServerContext.Key<EventSyncPubSubHolder> SUBSCRIBERS_HOLDER_KEY =
            new LanguageServerContext.Key<>();
    private static final Map<EventKind, EventPublisher> publisherMap = new HashMap<>();

    private EventSyncPubSubHolder(LanguageServerContext serverContext) {
        LSClientLogger lsClientLogger = LSClientLogger.getInstance(serverContext);
        serverContext.put(SUBSCRIBERS_HOLDER_KEY, this);
        initialize(lsClientLogger);
    }

    private void initialize(LSClientLogger lsClientLogger) {
        Map<EventKind, List<EventSubscriber>> eventSubscribersMap = new HashMap<>();
        ServiceLoader<EventSubscriber> subscribers = ServiceLoader.load(EventSubscriber.class);
        for (EventSubscriber eventSubscriber : subscribers) {
            EventKind eventKind = eventSubscriber.eventKind();
            eventSubscribersMap.computeIfAbsent(eventKind, k -> new ArrayList<>());
            eventSubscribersMap.get(eventKind).add(eventSubscriber);
        }

        ServiceLoader<EventPublisher> publishers = ServiceLoader.load(EventPublisher.class);
        publishers.forEach(eventPublisher -> {
            for (EventSubscriber eventSubscriber: eventSubscribersMap.get(eventPublisher.getKind())) {
                publisherMap.put(eventPublisher.getKind(), eventPublisher);
                eventPublisher.subscribe(eventSubscriber);
                lsClientLogger.logTrace(String.format("%s subscribed to %s", eventSubscriber.getName(),
                        eventPublisher.getName()));
            }
        });
    }

    /**
     * Returns the instance of Holder.
     *
     * @return subscriber holder instance
     */
    public static EventSyncPubSubHolder getInstance(LanguageServerContext serverContext) {
        EventSyncPubSubHolder subscribersHolder = serverContext.get(SUBSCRIBERS_HOLDER_KEY);
        if (subscribersHolder == null) {
            subscribersHolder = new EventSyncPubSubHolder(serverContext);
        }

        return subscribersHolder;
    }
    
    public EventPublisher getPublisher(EventKind eventKind) throws EventSyncException {
        if (!publisherMap.containsKey(eventKind)) {
            throw new EventSyncException("No publishers for the event kind");
        }
        return publisherMap.get(eventKind);
    }
    
    public List<EventPublisher> getPublishers(List<EventKind> eventKinds) throws EventSyncException {
        List<EventPublisher> eventPublishers = new ArrayList<>();
        for (EventKind eventKind : eventKinds) {
            if (!publisherMap.containsKey(eventKind)) {
                throw new EventSyncException("No publishers for the event kind");
            }
            eventPublishers.add(publisherMap.get(eventKind));
        }
        return eventPublishers;
    }
}
