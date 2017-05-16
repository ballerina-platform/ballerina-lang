/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.siddhi.extension.input.transport.jms;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.jms.exception.JMSConnectorException;
import org.wso2.carbon.transport.jms.receiver.JMSServerConnector;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSOptionsMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension(
        name = "jms",
        namespace = "source",
        description = "JMS Source",
        examples = @Example(description = "TBD", syntax = "TBD")
)
/**
 * JMS Source implementation.
 */
public class JMSSource extends Source {
    private static final Logger log = Logger.getLogger(JMSSource.class);
    private final int DEFAULT_THREAD_POOL_SIZE = 1;
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private JMSServerConnector jmsServerConnector;
    private JMSMessageProcessor jmsMessageProcessor;
    private int threadPoolSize;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ConfigReader configReader, ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        // todo: thread pool size should be read from the configuration file, since it's not available at the time of
        // this impl, it's hardcoded.
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        Map<String, String> properties = initJMSProperties();
        jmsServerConnector = new JMSServerConnector(properties);
        jmsMessageProcessor = new JMSMessageProcessor(sourceEventListener, threadPoolSize);
        jmsServerConnector.setMessageProcessor(jmsMessageProcessor);
        try {
            jmsServerConnector.start();
        } catch (ServerConnectorException e) {
            log.error("Exception in starting the JMS receiver for stream: "
                    + sourceEventListener.getStreamDefinition().getId(), e);
        }
    }

    @Override
    public void disconnect() {
        try {
            jmsServerConnector.stop();
            jmsMessageProcessor.disconnect();
        } catch (JMSConnectorException e) {
            log.error("Error disconnecting the JMS receiver", e);
        }
    }

    @Override
    public void destroy() {
        // disconnect() gets called before destroy() which does the cleanup destroy() needs
    }

    @Override
    public void pause() {
        jmsMessageProcessor.pause();
    }

    @Override
    public void resume() {
        jmsMessageProcessor.resume();
    }

    /**
     * Initializing JMS properties.
     * The properties in the required options list are mandatory.
     * Other JMS options can be passed in as key value pairs, key being in the JMS spec or the broker spec.
     *
     * @return all the options map.
     */
    private Map<String, String> initJMSProperties() {
        Map<String, String> customPropertyMapping = JMSOptionsMapper.getCustomPropertyMapping();
        List<String> requiredOptions = JMSOptionsMapper.getRequiredOptions();
        // getting the required values
        Map<String, String> transportProperties = new HashMap<>();
        requiredOptions.forEach(requiredOption ->
                transportProperties.put(customPropertyMapping.get(requiredOption),
                        optionHolder.validateAndGetStaticValue(requiredOption)));
        // getting optional values
        optionHolder.getStaticOptionsKeys().stream()
                .filter(option -> !requiredOptions.contains(option) && !option.equals("type")).forEach(option ->
                transportProperties.put(option, optionHolder.validateAndGetStaticValue(option)));
        return transportProperties;
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // no state to restore
    }
}
