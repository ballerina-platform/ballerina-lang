/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateLimiter;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.core.stream.input.source.InputMapper;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SubscriptionRuntime;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.extension.holder.InputMapperExecutorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.InputTransportExecutorExtensionHolder;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.EventWindow;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.Subscription;
import org.wso2.siddhi.query.api.execution.io.map.AttributeMapping;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;
import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubscriptionParser {
// TODO: 11/23/16 fix this

    /**
     * Parse a subscription and return corresponding QueryRuntime.
     *
     * @param subscription         subscription to be parsed.
     * @param executionPlanContext associated Execution Plan context.
     * @param streamDefinitionMap  keyvalue containing user given stream definitions.
     * @param tableDefinitionMap   keyvalue containing table definitions.
     * @param eventTableMap        keyvalue containing event tables.
     * @param eventSourceMap
     *@param eventSinkMap @return SubscriptionRuntime.
     */
    public static SubscriptionRuntime parse(final Subscription subscription, ExecutionPlanContext executionPlanContext,
                                            Map<String, AbstractDefinition> streamDefinitionMap,
                                            Map<String, AbstractDefinition> tableDefinitionMap,
                                            Map<String, AbstractDefinition> windowDefinitionMap,
                                            Map<String, EventTable> eventTableMap,
                                            Map<String, EventWindow> eventWindowMap,
                                            Map<String, List<InputTransport>> eventSourceMap,
                                            Map<String, List<OutputTransport>> eventSinkMap,
                                            LockSynchronizer lockSynchronizer) {
        SubscriptionRuntime subscriptionRuntime;
        String subscriptionName = null;
        Element nameElement = null;
        LatencyTracker latencyTracker = null;
        LockWrapper lockWrapper = null;
        try {
            nameElement = AnnotationHelper.getAnnotationElement("info", "name", subscription.getAnnotations());
            if (nameElement != null) {
                subscriptionName = nameElement.getValue();
            } else {
                subscriptionName = "subscription_" + UUID.randomUUID().toString();
            }
            if (executionPlanContext.isStatsEnabled() && executionPlanContext.getStatisticsManager() != null) {
                if (nameElement != null) {
                    String metricName =
                            executionPlanContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                                    SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                                    SiddhiConstants.METRIC_DELIMITER + executionPlanContext.getName() +
                                    SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI +
                                    SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_QUERIES +
                                    SiddhiConstants.METRIC_DELIMITER + subscriptionName;
                    latencyTracker = executionPlanContext.getSiddhiContext()
                            .getStatisticsConfiguration()
                            .getFactory()
                            .createLatencyTracker(metricName, executionPlanContext.getStatisticsManager());
                }
            }

            Extension transportExtension = new Extension() {
                @Override
                public String getNamespace() {
                    return SiddhiConstants.INPUT_TRANSPORT;
                }

                @Override
                public String getName() {
                    return subscription.getTransport().getType();
                }
            };

            InputTransport inputTransport = (InputTransport) SiddhiClassLoader.loadExtensionImplementation
                    (transportExtension, InputTransportExecutorExtensionHolder.getInstance(executionPlanContext));

//            InputTransport inputTransport = (InputTransport) SiddhiClassLoader.loadSiddhiImplementation
//                    (transportExtension.getName(), InputTransport.class);

            Mapping mapping = subscription.getMapping();
            if (mapping == null) {
                // Subscription without mapping
                throw new ExecutionPlanValidationException("Subscription must have a mapping plan but " +
                        transportExtension.getName() + " subscription does not have a mapping plan");
            }

            // Named mapping and positional mapping cannot be together
            boolean namedMappingFound = false;
            boolean positionalMappingFound = false;
            for (AttributeMapping attributeMapping : mapping.getAttributeMappingList()) {
                if (attributeMapping.getRename() == null) {
                    positionalMappingFound = true;
                } else {
                    namedMappingFound = true;
                }
                if (namedMappingFound && positionalMappingFound) {
                    throw new ExecutionPlanValidationException("Subscription mapping cannot have both named mapping " +
                            "and positional mapping together but " + mapping.getFormat() + " mapping uses both of " +
                            "them");
                }
            }

            Extension mapperExtension = new Extension() {
                @Override
                public String getNamespace() {
                    return SiddhiConstants.INPUT_MAPPER;
                }

                @Override
                public String getName() {
                    return subscription.getMapping().getFormat();
                }
            };

            InputMapper inputMapper = (InputMapper) SiddhiClassLoader.loadExtensionImplementation(mapperExtension,
                    InputMapperExecutorExtensionHolder.getInstance(executionPlanContext));

            StreamDefinition outputStreamDefinition = (StreamDefinition) streamDefinitionMap.get(subscription
                    .getOutputStream().getId());
            if (outputStreamDefinition == null) {
                outputStreamDefinition = (StreamDefinition) windowDefinitionMap.get(subscription.getOutputStream()
                        .getId());
            }

            if (outputStreamDefinition == null) {
                // Cannot infer the output stream
                throw new ExecutionPlanValidationException("Subscription must have an output stream or event window " +
                        "but " + transportExtension.getName() + " subscription does not have output stream");
            }

            OutputCallback outputCallback = OutputParser.constructOutputCallback(subscription.getOutputStream(),
                    outputStreamDefinition, eventTableMap, eventWindowMap,
                    executionPlanContext, false, subscriptionName);

            MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
            metaStreamEvent.setOutputDefinition(outputStreamDefinition);
            outputStreamDefinition.getAttributeList().forEach(metaStreamEvent::addOutputData);
            //todo annotation event creator and pass to init()
            inputMapper.init(outputStreamDefinition, outputCallback, metaStreamEvent, subscription.getMapping()
                    .getOptions(), subscription.getMapping().getAttributeMappingList());

            inputTransport.init(subscription.getTransport().getOptions(), inputMapper);

            OutputRateLimiter outputRateLimiter = OutputParser.constructOutputRateLimiter(subscription
                            .getOutputStream().getId(),
                    subscription.getOutputRate(), false, false, executionPlanContext.getScheduledExecutorService(),
                    executionPlanContext, subscriptionName);
            subscriptionRuntime = new SubscriptionRuntime(inputTransport, inputMapper, outputRateLimiter,
                    outputCallback);

            executionPlanContext.addEternalReferencedHolder(inputTransport);
            executionPlanContext.addEternalReferencedHolder(outputRateLimiter);

            if (outputRateLimiter instanceof WrappedSnapshotOutputRateLimiter) {
                throw new ExecutionPlanCreationException("Snapshot rate limiting not supported in subscription of " +
                        "name:" +
                        nameElement + " type:" + subscription.getTransport().getType());
            }
            outputRateLimiter.init(executionPlanContext, null, subscriptionName);


            subscriptionRuntime.init(subscription.getTransport().getOptions(), executionPlanContext);

        } catch (DuplicateDefinitionException e) {
            if (nameElement != null) {
                throw new DuplicateDefinitionException(e.getMessage() + ", when creating subscription " + subscriptionName, e);
            } else {
                throw new DuplicateDefinitionException(e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            if (nameElement != null) {
                throw new ExecutionPlanCreationException(e.getMessage() + ", when creating subscription " + subscriptionName, e);
            } else {
                throw new ExecutionPlanCreationException(e.getMessage(), e);
            }
        }
        return subscriptionRuntime;
    }
}
