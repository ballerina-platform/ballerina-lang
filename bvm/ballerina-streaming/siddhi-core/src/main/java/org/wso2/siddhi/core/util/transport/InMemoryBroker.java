/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory broker to support in-memory transport.
 */
public class InMemoryBroker {
    private static final MessageBroker broker = new MessageBroker();

    public static void subscribe(Subscriber subscriber) {
        broker.register(subscriber);
    }

    public static void unsubscribe(Subscriber subscriber) {
        broker.unregister(subscriber);
    }

    public static void publish(String topic, Object message) {
        broker.publish(topic, message);
    }

    interface Broker {
        void register(Subscriber subscriber);

        void unregister(Subscriber subscriber);

        void broadcast(String topic, Object msg);
    }

    /**
     * Subscriber interface to be implemented to subscribe to in-memory broker.
     */
    public interface Subscriber {
        void onMessage(Object msg);

        String getTopic();
    }

    private static class MessageBroker implements Broker {

        private final Object mutex = new Object();
        private Map<String, List<Subscriber>> topicSubscribers;

        public MessageBroker() {
            this.topicSubscribers = new HashMap<>();
        }

        @Override
        public void register(final Subscriber subscriber) {
            if (subscriber == null) {
                throw new NullPointerException("Subscriber cannot be null.");
            }
            synchronized (mutex) {
                if (topicSubscribers.containsKey(subscriber.getTopic())) {
                    if (!topicSubscribers.get(subscriber.getTopic()).contains(subscriber)) {
                        topicSubscribers.get(subscriber.getTopic()).add(subscriber);
                    }
                } else {
                    topicSubscribers.put(subscriber.getTopic(), new ArrayList<Subscriber>() {
                        {
                            add(subscriber);
                        }
                    });
                }
            }
        }

        @Override
        public void unregister(Subscriber subscriber) {
            synchronized (mutex) {
                try {
                    topicSubscribers.get(subscriber.getTopic()).remove(subscriber);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void broadcast(String topic, Object msg) {
            List<Subscriber> subscribers;
            // synchronization is used to make sure
            // any observer registered after message
            // is received is not notified
            synchronized (mutex) {
                if (this.topicSubscribers.containsKey(topic)) {
                    subscribers = new ArrayList<>(this.topicSubscribers.get(topic));
                    for (Subscriber subscriber : subscribers) {
                        subscriber.onMessage(msg);
                    }
                }
            }
        }

        public void publish(String topic, Object msg) {
            broadcast(topic, msg);
        }

    }
}
