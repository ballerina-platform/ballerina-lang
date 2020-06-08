/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.kafka.observability;

import org.ballerinalang.jvm.observability.ObserverContext;

/**
 * Extension of ObserverContext for Kafka.
 *
 * @since 1.2.0
 */
public class KafkaObserverContext extends ObserverContext {

    private KafkaObserverContext() {
        setObjectName(KafkaObservabilityConstants.CONNECTOR_NAME);
    }

    KafkaObserverContext(String context) {
        this();
        setObjectName(KafkaObservabilityConstants.CONNECTOR_NAME);
        addTag(KafkaObservabilityConstants.TAG_CONTEXT, context);
    }

    public KafkaObserverContext(String context, String clientId, String url) {
        this(context);
        addTag(KafkaObservabilityConstants.TAG_CLIENT_ID, clientId);
        addTag(KafkaObservabilityConstants.TAG_URL, url);
    }

    public KafkaObserverContext(String context, String clientId, String url, String topic) {
        this(context, clientId, url);
        addTag(KafkaObservabilityConstants.TAG_TOPIC, topic);
    }

}
