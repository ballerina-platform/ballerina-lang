/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nats.observability;

import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.Tags;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extension of ObserverContext for NATS.
 *
 * @since 1.1.0
 */
public class NatsObserverContext extends ObserverContext {

    NatsObserverContext() {
        setConnectorName(NatsObservabilityConstants.CONNECTOR_NAME);
    }

    NatsObserverContext(String context) {
        setConnectorName(NatsObservabilityConstants.CONNECTOR_NAME);
        addTag(NatsObservabilityConstants.TAG_CONTEXT, context);
    }

    NatsObserverContext(String context, String url) {
        this(context);
        addTag(NatsObservabilityConstants.TAG_URL, url);
    }

    public NatsObserverContext(String context, String url, String subject) {
        this(context, url);
        addTag(NatsObservabilityConstants.TAG_SUBJECT, subject);
    }

    Set<Tag> getAllTags() {
        Map<String, String> tags = getTags();
        Set<Tag> allTags = new HashSet<>(tags.size());
        Tags.tags(allTags, tags);
        return allTags;
    }

}
