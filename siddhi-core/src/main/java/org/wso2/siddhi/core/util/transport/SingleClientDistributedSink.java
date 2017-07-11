/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.stream.output.sink.distributed.DistributedTransport;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.extension.holder.SinkExecutorExtensionHolder;
import org.wso2.siddhi.core.util.parser.helper.DefinitionParserHelper;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.extension.Extension;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a the distributed sink that could publish to multiple destination using a single
 * client/publisher. Following are some examples,
 * - In a case where there are multiple partitions in a single topic in Kafka, the same kafka client can be used
 * to send events to all the partitions within the topic.
 * - The same email client can send email to different addresses.
 */
public class SingleClientDistributedSink extends DistributedTransport {

    private Sink sink;
    private int destinationCount = 0;

    @Override
    public void publish(Object payload, DynamicOptions transportOptions, int destinationId)
            throws ConnectionUnavailableException {
        try {
            transportOptions.setVariableOptionIndex(destinationId);
            sink.publish(payload, transportOptions);
        } catch (ConnectionUnavailableException e) {
            strategy.destinationFailed(destinationId);
            throw e;
        }
    }

    @Override
    public void initTransport(OptionHolder sinkOptionHolder, List<OptionHolder> destinationOptionHolders,
                              Annotation sinkAnnotation, ConfigReader sinkConfigReader,
                              SiddhiAppContext siddhiAppContext) {
        final String transportType = sinkOptionHolder.validateAndGetStaticValue(SiddhiConstants
                .ANNOTATION_ELEMENT_TYPE);
        Extension sinkExtension = DefinitionParserHelper.constructExtension
                (streamDefinition, SiddhiConstants.ANNOTATION_SINK, transportType, sinkAnnotation, SiddhiConstants
                        .NAMESPACE_SINK);

        Set<String> allDynamicOptionKeys = findAllDynamicOptions(destinationOptionHolders);
        destinationOptionHolders.forEach(optionHolder -> {
            optionHolder.merge(sinkOptionHolder);
            allDynamicOptionKeys.forEach(optionKey -> {
                String optionValue = optionHolder.getOrCreateOption(optionKey, null).getValue();
                if (optionValue == null || optionValue.isEmpty()) {
                    throw new SiddhiAppValidationException("Destination properties can only contain " +
                            "non-empty static values.");
                }

                Option sinkOption = sinkOptionHolder.getOrAddStaticOption(optionKey, optionValue);
                sinkOption.addVariableValue(optionValue);
                destinationCount++;
            });
        });

        this.sink = (Sink) SiddhiClassLoader.loadExtensionImplementation(
                sinkExtension, SinkExecutorExtensionHolder.getInstance(siddhiAppContext));
        this.sink.initOnlyTransport(streamDefinition, sinkOptionHolder, sinkConfigReader, siddhiAppContext);
    }

    @Override
    public Class[] getSupportedInputEventClasses() {
        return sink.getSupportedInputEventClasses();
    }

    /**
     * Will be called to connect to the backend before events are published
     *
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    @Override
    public void connect() throws ConnectionUnavailableException {
        sink.connect();
        for (int i = 0; i < destinationCount; i++) {
            strategy.destinationAvailable(i);
        }
    }

    /**
     * Will be called after all publishing is done, or when ConnectionUnavailableException is thrown
     */
    @Override
    public void disconnect() {
        sink.disconnect();
    }

    /**
     * Will be called at the end to clean all the resources consumed
     */
    @Override
    public void destroy() {
        sink.destroy();
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an array
     */
    @Override
    public Map<String, Object> currentState() {
        return sink.currentState();
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on
     *              the same order provided by currentState().
     */
    @Override
    public void restoreState(Map<String, Object> state) {
        sink.restoreState(state);
    }

    private Set<String> findAllDynamicOptions(List<OptionHolder> destinationOptionHolders) {
        Set<String> dynamicOptions = new HashSet<>();
        destinationOptionHolders.forEach(destinationOptionHolder -> {
            destinationOptionHolder.getDynamicOptionsKeys().forEach(dynamicOptions::add);
            destinationOptionHolder.getStaticOptionsKeys().forEach(dynamicOptions::add);
        });

        return dynamicOptions;
    }
}
