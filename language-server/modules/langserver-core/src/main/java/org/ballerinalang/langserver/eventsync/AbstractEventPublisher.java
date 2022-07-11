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

import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the language server abstract event publisher.
 *
 * @since 2201.1.1
 */
public abstract class AbstractEventPublisher implements EventPublisher {

    protected final List<EventSubscriber> subscribers = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<EventSubscriber> getSubscribers() {
        return subscribers;
    }

    @Override
    public void subscribe(EventSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }
}
