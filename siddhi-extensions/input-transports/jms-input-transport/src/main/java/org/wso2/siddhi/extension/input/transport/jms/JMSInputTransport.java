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

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSConnectionFactory;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSInputTransportConstants;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSListener;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSMessageListener;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSTaskManager;
import org.wso2.siddhi.extension.input.transport.jms.util.JMSTaskManagerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

@Extension(
        name = "jms",
        namespace = "inputtransport",
        description = ""
)
public class JMSInputTransport extends InputTransport {

    private static final Logger log = Logger.getLogger(JMSInputTransport.class);

    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private JMSConnectionFactory jmsConnectionFactory;
    private JMSListener jmsListener;
    private ExecutionPlanContext executionPlanContext;

    private int minThreadPoolSize;
    private int maxThreadPoolSize;
    private int jobQueueSize;
    private String destination;
    private int KeepAliveTimeInMillis;
    private String inputAdapterName;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        this.executionPlanContext = executionPlanContext;

        minThreadPoolSize = JMSInputTransportConstants.DEFAULT_MIN_THREAD_POOL_SIZE;
        maxThreadPoolSize = JMSInputTransportConstants.DEFAULT_MAX__THREAD_POOL_SIZE;
        //todo: get these from a config file
        jobQueueSize = JMSInputTransportConstants.DEFAULT_JOB_IN_QUEUE_SIZE;
        KeepAliveTimeInMillis = JMSInputTransportConstants.DEFAULT_KEEP_ALIVE_TIME_IN_INTERVAL;
        inputAdapterName = "JMS-INPUT-" + sourceEventListener.getStreamDefinition().getId();
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        Map<String, String> jmsConnectionProperties = initJMSProperties();
        destination = optionHolder.validateAndGetStaticValue(JMSInputTransportConstants.ADAPTER_JMS_DESTINATION);
        //creating the connection factory
        jmsConnectionFactory = new JMSConnectionFactory(convertMapToHashTable(jmsConnectionProperties),
                inputAdapterName);
        Map<String, String> messageConfig = new HashMap<>();
        messageConfig.put(JMSInputTransportConstants.ADAPTER_JMS_DESTINATION, destination);
        JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory,
                inputAdapterName, new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize, KeepAliveTimeInMillis,
                        jobQueueSize, "JMS Threads", "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManager.setJmsMessageListener(new JMSMessageListener(sourceEventListener));

        jmsListener = new JMSListener(inputAdapterName + "#" + destination, jmsTaskManager);
        jmsListener.startListener();    }

    @Override
    public void disconnect() {
        if (jmsListener != null) {
            jmsListener.stopListener();
        }

        if (jmsConnectionFactory != null) {
            jmsConnectionFactory.stop();
        }

        if (log.isDebugEnabled()) {
            log.debug("JMS consumer " + inputAdapterName + " disconnected from destination : " + destination);
        }
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
        Map<String, String> transportProperties = new HashMap<>();
        Map<String, String> jmsProperties = extractProperties(
                optionHolder.getOrCreateOption(JMSInputTransportConstants.ADAPTER_PROPERTIES, null).getValue());
        Map<String, String> secureJmsProperties = extractProperties(
                optionHolder.getOrCreateOption(JMSInputTransportConstants.ADAPTER_SECURED_PROPERTIES, null).getValue());
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_DESTINATION_TYPE,
                optionHolder.validateAndGetStaticValue(JMSInputTransportConstants.ADAPTER_JMS_DESTINATION_TYPE));
        transportProperties.put(JMSInputTransportConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS,
                optionHolder.validateAndGetStaticValue(JMSInputTransportConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        transportProperties.put(JMSInputTransportConstants.JAVA_NAMING_PROVIDER_URL,
                optionHolder.validateAndGetStaticValue(JMSInputTransportConstants.JAVA_NAMING_PROVIDER_URL));
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME,
                optionHolder.validateAndGetStaticValue(
                        JMSInputTransportConstants.ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME));
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_USERNAME,
                optionHolder.getOrCreateOption(JMSInputTransportConstants.ADAPTER_JMS_USERNAME, "").getValue());
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_PASSWORD,
                optionHolder.getOrCreateOption(JMSInputTransportConstants.ADAPTER_JMS_PASSWORD, "").getValue());
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_SUBSCRIPTION_DURABLE,
                optionHolder.getOrCreateOption(
                        JMSInputTransportConstants.ADAPTER_JMS_SUBSCRIPTION_DURABLE, "").getValue());
        transportProperties.put(JMSInputTransportConstants.ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID,
                optionHolder.getOrCreateOption(
                        JMSInputTransportConstants.ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID, "").getValue());
        if (jmsProperties != null) {
            transportProperties.putAll(jmsProperties);
            if (optionHolder.isOptionExists(JMSInputTransportConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS)) {
                try {
                    minThreadPoolSize = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                            JMSInputTransportConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS));
                } catch (NumberFormatException e) {
                    log.error("Invalid JMS Property: " + JMSInputTransportConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS +
                            ", ignoring configuration and using default for JMS output event adaptor...");
                }
            }
            if (optionHolder.isOptionExists(JMSInputTransportConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS)) {
                try {
                    maxThreadPoolSize = Integer.parseInt(optionHolder.validateAndGetStaticValue(
                            JMSInputTransportConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS));
                } catch (NumberFormatException e) {
                    log.error("Invalid JMS Property: " + JMSInputTransportConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS
                            + ", ignoring configuration and using default for JMS output event adaptor...");
                }
            }
        }
        if (secureJmsProperties != null) {
            transportProperties.putAll(secureJmsProperties);
        }
        return transportProperties;
    }

    private Map<String, String> extractProperties(String properties) {
        if (properties == null || properties.trim().length() == 0) {
            return null;
        }

        String[] entries = properties.split(JMSInputTransportConstants.PROPERTY_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String property : entries) {
            try {
                keyValue = property.split(JMSInputTransportConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("JMS property '" + property + "' is not defined in the correct format.", e);
            }
        }
        return result;
    }

    private Hashtable<String, String> convertMapToHashTable(Map<String, String> map) {
        Hashtable<String, String> table = new Hashtable<>();
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            Map.Entry pair = (Map.Entry) stringStringEntry;
            //null values will be removed
            if (pair.getValue() != null) {
                table.put(pair.getKey().toString(), pair.getValue().toString());
            }
        }
        return table;
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
