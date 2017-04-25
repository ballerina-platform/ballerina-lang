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
import org.wso2.carbon.transport.jms.receiver.JMSServerConnector;
import org.wso2.carbon.transport.jms.utils.JMSConstants;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSMessageProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension(
        name = "jms",
        namespace = "inputtransport",
        description = ""
)
public class JMSInputTransport extends InputTransport {
    private static final Logger log = Logger.getLogger(JMSInputTransport.class);

    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private ExecutionPlanContext executionPlanContext;
    private JMSServerConnector jmsServerConnector;

    private int minThreadPoolSize, maxThreadPoolSize;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        this.executionPlanContext = executionPlanContext;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        Map<String, String> properties = initJMSProperties();
        jmsServerConnector = new JMSServerConnector(properties);
        JMSMessageProcessor jmsMessageProcessor = new JMSMessageProcessor(sourceEventListener);
        jmsServerConnector.setMessageProcessor(jmsMessageProcessor);
        try {
            jmsServerConnector.start();
        } catch (ServerConnectorException e) {
            log.error("Exception in starting the JMS receiver for stream : "
                    + sourceEventListener.getStreamDefinition().getId(), e);
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void pause() {
        //todo: implement this
    }

    @Override
    public void resume() {
        //todo: implement this
    }

    private Map<String, String> initJMSProperties() {
        List<String> requiredOptions = Arrays.asList(JMSConstants.DESTINATION_PARAM_NAME,
                JMSConstants.CONNECTION_FACTORY_JNDI_PARAM_NAME, JMSConstants.NAMING_FACTORY_INITIAL_PARAM_NAME,
                JMSConstants.PROVIDER_URL_PARAM_NAME, JMSConstants.CONNECTION_FACTORY_TYPE_PARAM_NAME);
        // getting the required values
        Map<String, String> transportProperties = new HashMap<>();
        requiredOptions.stream().forEach(requiredOption -> {
            transportProperties.put(requiredOption, optionHolder.validateAndGetStaticValue(requiredOption));
        });
        // getting optional values
        optionHolder.getStaticOptionsKeys().stream()
                .filter(option -> !requiredOptions.contains(option) && !option.equals("type")).forEach(option -> {
            transportProperties.put(option, optionHolder.validateAndGetStaticValue(option));
        });
        return transportProperties;
    }

    @Override
    public Map<String, Object> currentState() {
        return new HashMap<>();
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //todo: implement this
    }
}
