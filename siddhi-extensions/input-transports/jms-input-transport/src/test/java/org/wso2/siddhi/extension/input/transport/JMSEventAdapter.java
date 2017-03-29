/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.extension.input.transport;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.SourceEventListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.jms.internal.util.*;
import org.wso2.siddhi.extension.input.transport.util.JMSConnectionFactory;
import org.wso2.siddhi.extension.input.transport.util.JMSConstants;
import org.wso2.siddhi.extension.input.transport.util.JMSEventAdapterConstants;
import org.wso2.siddhi.extension.input.transport.util.JMSListener;
import org.wso2.siddhi.extension.input.transport.util.JMSMessageListener;
import org.wso2.siddhi.extension.input.transport.util.JMSTaskManager;
import org.wso2.siddhi.extension.input.transport.util.JMSTaskManagerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class JMSEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private final String id = UUID.randomUUID().toString();
    private SourceEventListener eventAdapterListener;
    private JMSConnectionFactory jmsConnectionFactory;
    private JMSListener jmsListener;
    private Logger log = Logger.getLogger(JMSEventAdapter.class);
    private String destination;
    private int minThreadPoolSize;
    private int maxThreadPoolSize;
    private int KeepAliveTimeInMillis;
    private int jobQueueSize;


    public JMSEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;

        if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
            KeepAliveTimeInMillis = Integer.parseInt(globalProperties.get(
                    JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
        } else {
            KeepAliveTimeInMillis = JMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_INTERVAL;
        }

        if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
            jobQueueSize = Integer.parseInt(globalProperties.get(
                    JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
        } else {
            jobQueueSize = JMSEventAdapterConstants.DEFAULT_JOB_IN_QUEUE_SIZE;
        }

        minThreadPoolSize = JMSEventAdapterConstants.DEFAULT_MIN_THREAD_POOL_SIZE;
        maxThreadPoolSize = JMSEventAdapterConstants.DEFAULT_MAX__THREAD_POOL_SIZE;

    }


    @Override
    public void init(SourceEventListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        createJMSAdaptorListener(eventAdapterListener);
    }

    @Override
    public void disconnect() {
        if (jmsListener != null) {
            jmsListener.stopListener();
        }

        if (jmsConnectionFactory != null) {
            jmsConnectionFactory.stop();
        }

        if (log.isDebugEnabled()) {
            log.debug("JMS consumer " + eventAdapterConfiguration.getName() + " disconnected from destination : " + destination);
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JMSEventAdapter)) return false;
        JMSEventAdapter that = (JMSEventAdapter) o;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void createJMSAdaptorListener(
            SourceEventListener inputEventAdaptorListener) {

        Map<String, String> adapterProperties = new HashMap<String, String>();
        adapterProperties.putAll(eventAdapterConfiguration.getProperties());

        Map<String, String> jmsProperties = this.extractProperties(eventAdapterConfiguration.getProperties().get(
                JMSEventAdapterConstants.ADAPTER_PROPERTIES));

        Map<String, String> jmsSecuredProperties = this.extractProperties(eventAdapterConfiguration.getProperties().get(
                JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES));

        if (jmsProperties != null) {
            adapterProperties.remove(JMSEventAdapterConstants.ADAPTER_PROPERTIES);
            adapterProperties.putAll(jmsProperties);

            if (adapterProperties.containsKey(JMSEventAdapterConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS)) {
                try {
                    minThreadPoolSize = Integer.parseInt(adapterProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS));
                } catch (NumberFormatException e) {
                    log.error("Invalid JMS Property: " + JMSEventAdapterConstants.ADAPTER_JMS_CONCURRENT_CONSUMERS + ", "
                            + "ignoring configuration and using default for JMS output event adaptor...");
                }
            }

            if (adapterProperties.containsKey(JMSEventAdapterConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS)) {
                try {
                    maxThreadPoolSize = Integer.parseInt(adapterProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS));
                } catch (NumberFormatException e) {
                    log.error("Invalid JnikeMS Property: " + JMSEventAdapterConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS + ", "
                            + "ignoring configuration and using default for JMS output event adaptor...");
                }
            }
        }

        if(jmsSecuredProperties != null){
            adapterProperties.remove(JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES);
            adapterProperties.putAll(jmsSecuredProperties);
        }

        destination = eventAdapterConfiguration.getProperties().get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);

        jmsConnectionFactory = new JMSConnectionFactory(convertMapToHashTable(adapterProperties),
                eventAdapterConfiguration.getName());

        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
        JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory,
                eventAdapterConfiguration.getName(), new NativeWorkerPool(minThreadPoolSize, maxThreadPoolSize, KeepAliveTimeInMillis, jobQueueSize, "JMS Threads",
                "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManager.setJmsMessageListener(new JMSMessageListener(inputEventAdaptorListener));

        jmsListener = new JMSListener(eventAdapterConfiguration.getName() + "#" + destination,
                jmsTaskManager);
        jmsListener.startListener();

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

    private Map<String, String> extractProperties(String properties) {
        if (properties == null || properties.trim().length() == 0) {
            return null;
        }

        String[] entries = properties.split(JMSEventAdapterConstants.PROPERTY_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String property : entries) {
            try {
                keyValue = property.split(JMSEventAdapterConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("JMS property '" + property + "' is not defined in the correct format.", e);
            }
        }
        return result;

    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        String destinationType = eventAdapterConfiguration.getProperties().get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION_TYPE);
        return destinationType.equalsIgnoreCase(JMSEventAdapterConstants.DESTINATION_TOPIC);
    }

    @Override
    public boolean isPolling() {
        return true;
    }

}
