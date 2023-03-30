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

import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

import java.util.List;

/**
 * Represents the language server event publisher.
 *
 * @since 2201.1.1
 */
public interface EventPublisher {
    
    /**
     * Get the List of Event Subscribers.
     *
     * @return {@link List<EventSubscriber>} List of EventSubscribers.
     */
    List<EventSubscriber> getSubscribers();

    /**
     * @return kind of the publisher.
     */
    EventKind getKind();
    
    /**
     * @return name of the Event Publisher.
     */
    String getName();

    /**
     * Subscribe to an event subscriber to a publisher.
     * 
     * @param subscriber Event Subscriber
     */
    void subscribe(EventSubscriber subscriber);

    /**
     * Unsubscribe to an event subscriber to a publisher.
     *
     * @param subscriber Event Subscriber
     */
    void unsubscribe(EventSubscriber subscriber);

    /**
     * Publish events to subscribers.
     * 
     * @param client        Extended Language Client
     * @param serverContext Language Server Context
     * @param context       Document Service Context
     */
    void publish(ExtendedLanguageClient client, LanguageServerContext serverContext,
                 DocumentServiceContext context);
}
