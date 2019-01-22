/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.net.jms;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utility class for JMS related common operations.
 */
public class JmsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsUtils.class);

    /**
     * Utility class cannot be instantiated.
     */
    private JmsUtils() {
    }

    /**
     * Creates the JMS connector friendly properties Map. Converting properties as required.
     *
     * @param jmsConfig {@link Annotation}
     * @return Map of String key value properties.
     */
    public static Map<String, String> preProcessServiceConfig(Annotation jmsConfig) {
        Map<String, String> configParams = new HashMap<>();
        Struct configStruct = jmsConfig.getValue();
        if (Objects.isNull(configStruct)) {
            return configParams;
        }

        addStringParamIfPresent(JmsConstants.ALIAS_DESTINATION, configStruct, configParams);
        addStringParamIfPresent(JmsConstants.ALIAS_CONNECTION_FACTORY_NAME, configStruct, configParams);
        addStringParamIfPresent(JmsConstants.ALIAS_DESTINATION_TYPE, configStruct, configParams);
        addStringParamIfPresent(JmsConstants.ALIAS_CLIENT_ID, configStruct, configParams);
        addStringParamIfPresent(JmsConstants.ALIAS_DURABLE_SUBSCRIBER_ID, configStruct, configParams);
        addStringParamIfPresent(JmsConstants.ALIAS_ACK_MODE, configStruct, configParams);

        preProcessMapField(configParams, configStruct.getMapField(JmsConstants.PROPERTIES_MAP));
        return configParams;
    }

    public static Connection createConnection(Struct connectionConfig) {
        Map<String, String> configParams = new HashMap<>();

        String initialContextFactory = connectionConfig.getStringField(JmsConstants.ALIAS_INITIAL_CONTEXT_FACTORY);
        configParams.put(JmsConstants.ALIAS_INITIAL_CONTEXT_FACTORY, initialContextFactory);

        String providerUrl = connectionConfig.getStringField(JmsConstants.ALIAS_PROVIDER_URL);
        configParams.put(JmsConstants.ALIAS_PROVIDER_URL, providerUrl);

        String factoryName = connectionConfig.getStringField(JmsConstants.ALIAS_CONNECTION_FACTORY_NAME);
        configParams.put(JmsConstants.ALIAS_CONNECTION_FACTORY_NAME, factoryName);

        preProcessIfWso2MB(configParams);
        updateMappedParameters(configParams);

        Properties properties = new Properties();
        configParams.forEach(properties::put);

        //check for additional jndi properties
        Map<String, Value> props = connectionConfig.getMapField(JmsConstants.PROPERTIES_MAP);
        if (props != null) {
            for (Map.Entry<String, Value> entry : props.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().getStringValue());
            }
        }

        try {
            InitialContext initialContext = new InitialContext(properties);
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup(factoryName);
            String username = null;
            String password = null;
            if (connectionConfig.getRefField(JmsConstants.ALIAS_USERNAME) != null &&
                    connectionConfig.getRefField(JmsConstants.ALIAS_PASSWORD) != null) {
                username = connectionConfig.getRefField(JmsConstants.ALIAS_USERNAME).getStringValue();
                password = connectionConfig.getRefField(JmsConstants.ALIAS_PASSWORD).getStringValue();
            }

            if (!JmsUtils.isNullOrEmptyAfterTrim(username) && password != null) {
                return connectionFactory.createConnection(username, password);
            } else {
                return connectionFactory.createConnection();
            }
        } catch (NamingException | JMSException e) {
            String message = "Error while connecting to broker.";
            LOGGER.error(message, e);
            throw new BallerinaException(message + " " + e.getMessage(), e);
        }
    }

    public static Session createSession(Connection connection, Struct sessionConfig) {

        int sessionAckMode;
        boolean transactedSession = false;

        String ackModeString = sessionConfig.getStringField(JmsConstants.ALIAS_ACK_MODE);

        switch (ackModeString) {
            case JmsConstants.CLIENT_ACKNOWLEDGE_MODE:
                sessionAckMode = Session.CLIENT_ACKNOWLEDGE;
                break;
            case JmsConstants.SESSION_TRANSACTED_MODE:
                sessionAckMode = Session.SESSION_TRANSACTED;
                transactedSession = true;
                break;
            case JmsConstants.DUPS_OK_ACKNOWLEDGE_MODE:
                sessionAckMode = Session.DUPS_OK_ACKNOWLEDGE;
                break;
            case JmsConstants.AUTO_ACKNOWLEDGE_MODE:
                sessionAckMode = Session.AUTO_ACKNOWLEDGE;
                break;
            default:
                throw new BallerinaException("Unknown acknowledgment mode: " + ackModeString);
        }

        try {
            return connection.createSession(transactedSession, sessionAckMode);
        } catch (JMSException e) {
            String message = "Error while creating session.";
            LOGGER.error(message, e);
            throw new BallerinaException(message + " " + e.getMessage(), e);
        }
    }

    public static boolean isNullOrEmptyAfterTrim(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static void preProcessIfWso2MB(Map<String, String> configParams) {
        String initialConnectionFactoryName = configParams.get(JmsConstants.ALIAS_INITIAL_CONTEXT_FACTORY);
        if (JmsConstants.BMB_ICF_ALIAS.equalsIgnoreCase(initialConnectionFactoryName)
                || JmsConstants.MB_ICF_ALIAS.equalsIgnoreCase(initialConnectionFactoryName)) {

            configParams.put(JmsConstants.ALIAS_INITIAL_CONTEXT_FACTORY, JmsConstants.MB_ICF_NAME);
            String connectionFactoryName = configParams.get(JmsConstants.ALIAS_CONNECTION_FACTORY_NAME);
            if (configParams.get(JmsConstants.ALIAS_PROVIDER_URL) != null) {
                System.setProperty("qpid.dest_syntax", "BURL");
                if (!isNullOrEmptyAfterTrim(connectionFactoryName)) {
                    configParams.put(JmsConstants.MB_CF_NAME_PREFIX + connectionFactoryName,
                                     configParams.get(JmsConstants.ALIAS_PROVIDER_URL));
                    configParams.remove(JmsConstants.ALIAS_PROVIDER_URL);
                } else {
                    throw new BallerinaException(
                            JmsConstants.ALIAS_CONNECTION_FACTORY_NAME + " property should be set");
                }
            } else if (configParams.get(JmsConstants.CONFIG_FILE_PATH) != null) {
                configParams.put(JmsConstants.ALIAS_PROVIDER_URL, configParams.get(JmsConstants.CONFIG_FILE_PATH));
                configParams.remove(JmsConstants.CONFIG_FILE_PATH);
            }
        }
    }

    public static void updateMappedParameters(Map<String, String> configParams) {
        Iterator<Map.Entry<String, String>> iterator = configParams.entrySet().iterator();
        Map<String, String> tempMap = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String mappedParam = JmsConstants.MAPPING_PARAMETERS.get(entry.getKey());
            if (mappedParam != null) {
                tempMap.put(mappedParam, entry.getValue());
                iterator.remove();
            }
        }
        configParams.putAll(tempMap);
    }

    private static void addStringParamIfPresent(String paramName, Struct configStruct, Map<String, String> paramsMap) {
        String param;
        param = configStruct.getStringField(paramName);
        if (Objects.nonNull(param) && !param.isEmpty()) {
            paramsMap.put(paramName, param);
        }
    }

    /**
     * Process the provided properties in the {@link Map} and convert it to jms connector friendly Map.
     *
     * @param configParams Map instance that is getting filled.
     * @param properties   {@link Map} of properties.
     */
    private static void preProcessMapField(Map<String, String> configParams, Map<String, Value> properties) {

        if (Objects.isNull(properties)) {
            return;
        }

        for (Map.Entry<String, Value> entry : properties.entrySet()) {
            configParams.put(entry.getKey(), entry.getValue().getStringValue());
        }
    }

    /**
     * Extract JMS Message from the struct.
     *
     * @param messageStruct ballerina struct.
     * @return {@link Message} instance located in struct.
     */
    public static Message getJMSMessage(BMap<String, BValue> messageStruct) {
        Object nativeData = messageStruct.getNativeData(JmsConstants.JMS_MESSAGE_OBJECT);
        if (nativeData instanceof Message) {
            return (Message) nativeData;
        } else {
            throw new BallerinaException("JMS message has not been created.");
        }
    }

    /**
     * Wrap JMS Message from BallerinaJmsMessage.
     *
     * @param message JMS transport message.
     * @return {@link BallerinaJmsMessage} wrapped message instance.
     */
    public static BallerinaJmsMessage buildBallerinaJMSMessage(Message message) {
        BallerinaJmsMessage ballerinaJMSMessage = new BallerinaJmsMessage(message);
        try {
            if (message.getJMSReplyTo() != null) {
                if (message.getJMSReplyTo() instanceof Queue) {
                    ballerinaJMSMessage.setReplyDestinationName(((Queue) message.getJMSReplyTo()).getQueueName());
                } else if (message.getJMSReplyTo() instanceof Topic) {
                    ballerinaJMSMessage.setReplyDestinationName(((Topic) message.getJMSReplyTo()).getTopicName());
                } else {
                    LOGGER.warn("ignore unexpected jms destination type received as ReplyTo header.");
                }
            }
        } catch (JMSException e) {
            throw new BallerinaException("error retrieving reply destination from the message. " + e.getMessage(), e);
        }
        return ballerinaJMSMessage;
    }

    /**
     * Extract JMS Resource from the Ballerina Service.
     *
     * @param service Service instance.
     * @return extracted resource.
     */
    public static Resource extractJMSResource(Service service) {
        Resource[] resources = service.getResources();
        if (resources.length == 0) {
            throw new BallerinaException("No resources found to handle the JMS message in " + service.getName());
        }
        if (resources.length > 1) {
            throw new BallerinaException("More than one resources found in JMS service " + service.getName()
                    + ". JMS Service should only have one resource");
        }
        return resources[0];
    }

    public static Topic getTopic(Session session, String topicPattern) throws JMSException {
        return session.createTopic(topicPattern);
    }

    /**
     * Extract JMS Destination from the Destination struct.
     *
     * @param context ballerina context.
     * @param destinationBObject Destination struct.
     * @return JMS Destination object or null.
     */
    public static Destination getDestination(Context context, BMap<String, BValue> destinationBObject) {
        Destination destination = null;
        Object destinationObject = destinationBObject != null ?
                destinationBObject.getNativeData(JmsConstants.JMS_DESTINATION_OBJECT) : null;
        if (destinationObject != null) {
            destination = BallerinaAdapter.getNativeObject(destinationBObject,
                                                           JmsConstants.JMS_DESTINATION_OBJECT,
                                                           Destination.class,
                                                           context);
        }
        return destination;
    }

    /**
     * Extract queue name from the config struct.
     *
     * @param configBRecord config struct.
     * @return queue name or null.
     */
    public static String getQueueName(Struct configBRecord) {
        Value queueNameValue = configBRecord.getRefField(JmsConstants.QUEUE_SENDER_FIELD_QUEUE_NAME);
        return queueNameValue != null ? queueNameValue.getStringValue() : null;
    }

    /**
     * Extract topic name from the config struct.
     *
     * @param topicConfig config struct.
     * @return queue name or null.
     */
    public static String getTopicPattern(Struct topicConfig) {
        Value topicPatternValue = topicConfig.getRefField(JmsConstants.TOPIC_PUBLISHER_FIELD_TOPIC_PATTERN);
        return topicPatternValue != null ? topicPatternValue.getStringValue() : null;
    }
}
