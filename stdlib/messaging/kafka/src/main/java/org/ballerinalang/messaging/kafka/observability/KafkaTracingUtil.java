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

import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;

import java.util.Optional;

/**
 * Providing tracing functionality to Kafka.
 *
 * @since 1.2.0
 */
public class KafkaTracingUtil {

    public static void traceResourceInvocation(Strand strand, ObjectValue object, String topic) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext;
        Optional<ObserverContext> observerContextOptional = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        if (observerContextOptional.isPresent()) {
            observerContext = observerContextOptional.get();
        } else {
            observerContext = new ObserverContext();
            ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext);
        }
        setTags(observerContext, object, topic);
    }

    public static void traceResourceInvocation(Strand strand, ObjectValue object) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext;
        Optional<ObserverContext> observerContextOptional = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        if (observerContextOptional.isPresent()) {
            observerContext = observerContextOptional.get();
        } else {
            observerContext = new ObserverContext();
            ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext);
        }
        setTags(observerContext, object);
    }

    private static void setTags(ObserverContext observerContext, ObjectValue object, String topic) {
        observerContext.addTag(KafkaObservabilityConstants.TAG_URL, KafkaUtils.getBootstrapServers(object));
        observerContext.addTag(KafkaObservabilityConstants.TAG_CLIENT_ID, KafkaUtils.getClientId(object));
        observerContext.addTag(KafkaObservabilityConstants.TAG_TOPIC, topic);
    }

    private static void setTags(ObserverContext observerContext, ObjectValue object) {
        observerContext.addTag(KafkaObservabilityConstants.TAG_URL, KafkaUtils.getBootstrapServers(object));
        observerContext.addTag(KafkaObservabilityConstants.TAG_CLIENT_ID, KafkaUtils.getClientId(object));
    }

    private KafkaTracingUtil() {
    }
}
