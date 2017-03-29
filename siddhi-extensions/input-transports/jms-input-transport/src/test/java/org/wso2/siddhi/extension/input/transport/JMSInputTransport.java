/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.input.transport;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.extension.input.transport.util.JMSConnectionFactory;
import org.wso2.siddhi.extension.input.transport.util.JMSConstants;
import org.wso2.siddhi.extension.input.transport.util.JMSEventAdapterConstants;
import org.wso2.siddhi.extension.input.transport.util.JMSListener;
import org.wso2.siddhi.extension.input.transport.util.JMSMessageListener;
import org.wso2.siddhi.extension.input.transport.util.JMSTaskManager;
import org.wso2.siddhi.extension.input.transport.util.JMSTaskManagerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Extension(
        name = "jms",
        namespace = "inputtransport",
        description = ""
)
public class JMSInputTransport extends InputTransport {

    private String destination;
    private int minThreadPoolSize;
    private int maxThreadPoolSize;
    private int KeepAliveTimeInMillis;
    private int jobQueueSize;
    private SourceEventListener sourceEventListener = null;
    private OptionHolder optionHolder = null;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder) {
        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        if (optionHolder.validateAndGetOption(JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
            KeepAliveTimeInMillis = Integer.parseInt(optionHolder.validateAndGetOption(
                    JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME).getValue());
        } else {
            KeepAliveTimeInMillis = JMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_INTERVAL;
        }

        if (optionHolder.validateAndGetOption(JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
            jobQueueSize = Integer.parseInt(optionHolder.validateAndGetOption(
                    JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME).getValue());
        } else {
            jobQueueSize = JMSEventAdapterConstants.DEFAULT_JOB_IN_QUEUE_SIZE;
        }

        minThreadPoolSize = JMSEventAdapterConstants.DEFAULT_MIN_THREAD_POOL_SIZE;
        maxThreadPoolSize = JMSEventAdapterConstants.DEFAULT_MAX__THREAD_POOL_SIZE;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }

    private void createJMSAdaptorListener(SourceEventListener sourceEventListener) {

        Map<String, String> adapterProperties = new HashMap<String, String>();
        adapterProperties.putAll(this.optionHolder);

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
                    log.error("Invalid JMS Property: " + JMSEventAdapterConstants.ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS + ", "
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
        jmsTaskManager.setJmsMessageListener(new JMSMessageListener(sourceEventListener));

        jmsListener = new JMSListener(eventAdapterConfiguration.getName() + "#" + destination,
                jmsTaskManager);
        jmsListener.startListener();

    }
}
