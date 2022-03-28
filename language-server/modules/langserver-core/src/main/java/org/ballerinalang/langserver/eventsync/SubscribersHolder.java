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

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Subscribes and provide the Event Subscribers.
 *
 * @since 2201.1.0
 */
public class SubscribersHolder {
    private static final List<EventSubscriber> event_subscribers = new ArrayList<>();
    private static final LanguageServerContext.Key<SubscribersHolder> SUBSCRIBERS_HOLDER_KEY =
            new LanguageServerContext.Key<>();

    private SubscribersHolder(LanguageServerContext serverContext) {
        serverContext.put(SUBSCRIBERS_HOLDER_KEY, this);
        loadServices();
    }

    private void loadServices() {
        if (!SubscribersHolder.event_subscribers.isEmpty()) {
            return;
        }
        ServiceLoader<EventSubscriber> subscribers = ServiceLoader.load(EventSubscriber.class);
        for (EventSubscriber eventSubscriber : subscribers) {
            if (eventSubscriber == null) {
                continue;
            }
            SubscribersHolder.event_subscribers.add(eventSubscriber);
        }
    }

    /**
     * Returns the instance of Holder.
     *
     * @return subscriber holder instance
     */
    public static SubscribersHolder getInstance(LanguageServerContext serverContext) {
        SubscribersHolder subscribersHolder = serverContext.get(SUBSCRIBERS_HOLDER_KEY);
        if (subscribersHolder == null) {
            subscribersHolder = new SubscribersHolder(serverContext);
        }

        return subscribersHolder;
    }

    /**
     * Get the list of all active subscribers.
     *
     * @return {@link List} Subscribers List
     */
    public List<EventSubscriber> getSubscribers() {
        List<EventSubscriber> subscribersList = new ArrayList<>();
        for (EventSubscriber eventSubscriber : event_subscribers) {
            if (eventSubscriber != null) {
                subscribersList.add(eventSubscriber);
            }
        }
        return subscribersList;
    }
}
