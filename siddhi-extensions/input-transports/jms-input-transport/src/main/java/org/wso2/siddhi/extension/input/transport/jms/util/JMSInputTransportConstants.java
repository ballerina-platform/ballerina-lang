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
package org.wso2.siddhi.extension.input.transport.jms.util;

public class JMSInputTransportConstants {

    public static final String ADAPTER_TYPE_JMS = "jms";

    public static final String JNDI_INITIAL_CONTEXT_FACTORY_CLASS = "java.naming.factory.initial";
    public static final String JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT = "java.naming.factory.initial.hint";
    public static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";
    public static final String JAVA_NAMING_PROVIDER_URL_HINT = "java.naming.provider.url.hint";
    public static final String ADAPTER_JMS_USERNAME = "transport.jms.UserName";
    public static final String ADAPTER_JMS_PASSWORD = "transport.jms.Password";
    public static final String ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME = "transport.jms.ConnectionFactoryJNDIName";
    public static final String ADAPTER_JMS_CONNECTION_FACTORY_JNDINAME_HINT =
            "transport.jms.ConnectionFactoryJNDIName.hint";
    public static final String ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID = "transport.jms.DurableSubscriberClientID";
    public static final String ADAPTER_JMS_DURABLE_SUBSCRIBER_CLIENT_ID_HINT = "transport.jms.DurableSubscriberClientID.hint";
    public static final String ADAPTER_JMS_SUBSCRIPTION_DURABLE = "transport.jms.SubscriptionDurable";
    public static final String ADAPTER_JMS_SUBSCRIPTION_DURABLE_HINT = "transport.jms.SubscriptionDurable.hint";
    public static final String ADAPTER_JMS_DESTINATION_TYPE = "transport.jms.DestinationType";
    public static final String ADAPTER_JMS_DESTINATION_TYPE_HINT = "transport.jms.DestinationType.hint";

    public static final String ADAPTER_JMS_DESTINATION = "transport.jms.Destination";
    public static final String ADAPTER_JMS_DESTINATION_HINT = "transport.jms.Destination.hint";
    public static final String DESTINATION_TOPIC = "topic";
    public static final String DESTINATION_QUEUE = "queue";

    public static final int DEFAULT_MIN_THREAD_POOL_SIZE = 4;
    public static final int DEFAULT_MAX__THREAD_POOL_SIZE = 4;
    public static final int DEFAULT_KEEP_ALIVE_TIME_IN_INTERVAL = 1000;
    public static final int DEFAULT_JOB_IN_QUEUE_SIZE = 1000;
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_PROPERTIES = "jms.properties";
    public static final String ADAPTER_SECURED_PROPERTIES = "jms.secured.properties";
    public static final String ADAPTER_PROPERTIES_HINT = "jms.properties.hint";
    public static final String ADAPTER_SECURED_PROPERTIES_HINT = "jms.secured.properties.hint";
    public static final String PROPERTY_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";
    public static final String ADAPTER_JMS_CONCURRENT_CONSUMERS = "transport.jms.ConcurrentConsumers";
    public static final String ADAPTER_JMS_MAX_CONCURRENT_CONSUMERS = "transport.jms.MaxConcurrentConsumers";



}
