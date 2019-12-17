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

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nats.Constants;

import java.util.Optional;

/**
 * Providing metrics functionality to NATS.
 *
 * @since 1.1.0
 */
public class NatsTracingUtil {

    public static void traceResourceInvocation(Strand strand, String url, String subject) {
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
        setTags(observerContext, url, subject);
    }

    public static void traceResourceInvocation(Strand strand, String url) {
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
        setTags(observerContext, url);
    }

    public static void traceResourceInvocation(Strand strand, ObjectValue producerObject, String subject) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        Object connection = producerObject.get(Constants.CONNECTION_OBJ);
        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            String url = connectionObject.getStringValue(Constants.URL);
            traceResourceInvocation(strand, url, subject);
        } else {
            traceResourceInvocation(strand, NatsObservabilityConstants.UNKNOWN, subject);
        }
    }

    public static void traceResourceInvocation(Strand strand, ObjectValue listenerOrProducerObject) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        Object connection = listenerOrProducerObject.get(Constants.CONNECTION_OBJ);
        if (TypeChecker.getType(connection).getTag() == TypeTags.OBJECT_TYPE_TAG) {
            ObjectValue connectionObject = (ObjectValue) connection;
            String url = connectionObject.getStringValue(Constants.URL);
            traceResourceInvocation(strand, url);
        } else {
            traceResourceInvocation(strand, NatsObservabilityConstants.UNKNOWN);
        }
    }

    private static void setTags(ObserverContext observerContext, String url, String subject) {
        observerContext.addTag(NatsObservabilityConstants.TAG_URL, url);
        observerContext.addTag(NatsObservabilityConstants.TAG_SUBJECT, subject);
    }

    private static void setTags(ObserverContext observerContext, String url) {
        observerContext.addTag(NatsObservabilityConstants.TAG_URL, url);
    }

    private NatsTracingUtil() {
    }

}
