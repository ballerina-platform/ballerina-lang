/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.input.transport.jms.util;

import org.apache.axis2.transport.base.BaseUtils;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Miscallaneous methods used for the JMS transport
 */
public class JMSUtils extends BaseUtils {

    private static final Logger log = Logger.getLogger(JMSUtils.class);

    /**
     * Get a String property from the JMS message
     *
     * @param message  JMS message
     * @param property property name
     * @return property value
     */
    public static String getProperty(Message message, String property) {
        try {
            return message.getStringProperty(property);
        } catch (JMSException e) {
            return null;
        }
    }


    /**
     * Set the JMS ReplyTo for the message
     *
     * @param replyDestination the JMS Destination where the reply is expected
     * @param session          the session to use to create a temp Queue if a response is expected
     *                         but a Destination has not been specified
     * @param message          the JMS message where the final Destinatio would be set as the JMS ReplyTo
     * @return the JMS ReplyTo Destination for the message
     */
    public static Destination setReplyDestination(Destination replyDestination, Session session,
                                                  Message message) {

        if (replyDestination == null) {
            try {
                // create temporary queue to receive the reply
                replyDestination = createTemporaryDestination(session);
            } catch (JMSException e) {
                handleException("Error creating temporary queue for response", e);
            }
        }

        try {
            message.setJMSReplyTo(replyDestination);
        } catch (JMSException e) {
            log.warn("Error setting JMS ReplyTo destination to : " + replyDestination, e);
        }

        if (log.isDebugEnabled()) {
            try {
                assert replyDestination != null;
                log.debug("Expecting a response to JMS Destination : " +
                        (replyDestination instanceof Queue ?
                                ((Queue) replyDestination).getQueueName() :
                                ((Topic) replyDestination).getTopicName()));
            } catch (JMSException ignore) {
            }
        }
        return replyDestination;
    }

    /**
     * Set transport headers from the axis message context, into the JMS message
     *
     * @param messageConfiguration the adaptor message context
     * @param message              the JMS Message
     * @throws javax.jms.JMSException on exception
     */
    public static void setTransportHeaders(Map<String, String> messageConfiguration,
                                           Message message)
            throws JMSException {


        if (messageConfiguration == null) {
            return;
        }

        for (String name : messageConfiguration.keySet()) {

            if (name.startsWith(JMSConstants.JMSX_PREFIX) &&
                    !(name.equals(JMSConstants.JMSX_GROUP_ID) || name.equals(JMSConstants.JMSX_GROUP_SEQ))) {
                continue;
            }

            if (JMSConstants.JMS_COORELATION_ID.equals(name)) {
                message.setJMSCorrelationID(messageConfiguration.get(JMSConstants.JMS_COORELATION_ID));
            } else if (JMSConstants.JMS_DELIVERY_MODE.equals(name)) {
                String mode = messageConfiguration.get(JMSConstants.JMS_DELIVERY_MODE);
                try {
                    message.setJMSDeliveryMode(Integer.parseInt(mode));
                } catch (NumberFormatException nfe) {
                    log.warn("Invalid delivery mode ignored : " + mode, nfe);
                }

            } else if (JMSConstants.JMS_EXPIRATION.equals(name)) {
                message.setJMSExpiration(
                        Long.parseLong(messageConfiguration.get(JMSConstants.JMS_EXPIRATION)));
            } else if (JMSConstants.JMS_MESSAGE_ID.equals(name)) {
                message.setJMSMessageID(messageConfiguration.get(JMSConstants.JMS_MESSAGE_ID));
            } else if (JMSConstants.JMS_PRIORITY.equals(name)) {
                message.setJMSPriority(
                        Integer.parseInt(messageConfiguration.get(JMSConstants.JMS_PRIORITY)));
            } else if (JMSConstants.JMS_TIMESTAMP.equals(name)) {
                message.setJMSTimestamp(
                        Long.parseLong(messageConfiguration.get(JMSConstants.JMS_TIMESTAMP)));
            } else if (JMSConstants.JMS_MESSAGE_TYPE.equals(name)) {
                message.setJMSType(messageConfiguration.get(JMSConstants.JMS_MESSAGE_TYPE));

            } else {

                //TODO currently only string is supported  in messageConfiguration
                Object value = messageConfiguration.get(name);
                if (value instanceof String) {
                    message.setStringProperty(name, (String) value);
                } else if (value instanceof Boolean) {
                    message.setBooleanProperty(name, (Boolean) value);
                } else if (value instanceof Integer) {
                    message.setIntProperty(name, (Integer) value);
                } else if (value instanceof Long) {
                    message.setLongProperty(name, (Long) value);
                } else if (value instanceof Double) {
                    message.setDoubleProperty(name, (Double) value);
                } else if (value instanceof Float) {
                    message.setFloatProperty(name, (Float) value);
                }
            }
        }
    }

    /**
     * Extract transport level headers for JMS from the given message into a Map
     *
     * @param message the JMS message
     * @return a Map of the transport headers
     */
    public static Map<String, Object> getTransportHeaders(Message message) {
        // create a Map to hold transport headers
        Map<String, Object> map = new HashMap<String, Object>();

        // correlation ID
        try {
            if (message.getJMSCorrelationID() != null) {
                map.put(JMSConstants.JMS_COORELATION_ID, message.getJMSCorrelationID());
            }
        } catch (JMSException ignore) {
        }

        // set the delivery mode as persistent or not
        try {
            map.put(JMSConstants.JMS_DELIVERY_MODE, Integer.toString(message.getJMSDeliveryMode()));
        } catch (JMSException ignore) {
        }

        // destination name
        try {
            if (message.getJMSDestination() != null) {
                Destination dest = message.getJMSDestination();
                map.put(JMSConstants.JMS_DESTINATION,
                        dest instanceof Queue ?
                                ((Queue) dest).getQueueName() : ((Topic) dest).getTopicName());
            }
        } catch (JMSException ignore) {
        }

        // expiration
        try {
            map.put(JMSConstants.JMS_EXPIRATION, Long.toString(message.getJMSExpiration()));
        } catch (JMSException ignore) {
        }

        // if a JMS message ID is found
        try {
            if (message.getJMSMessageID() != null) {
                map.put(JMSConstants.JMS_MESSAGE_ID, message.getJMSMessageID());
            }
        } catch (JMSException ignore) {
        }

        // priority
        try {
            map.put(JMSConstants.JMS_PRIORITY, Long.toString(message.getJMSPriority()));
        } catch (JMSException ignore) {
        }

        // redelivered
        try {
            map.put(JMSConstants.JMS_REDELIVERED, Boolean.toString(message.getJMSRedelivered()));
        } catch (JMSException ignore) {
        }

        // replyto destination name
        try {
            if (message.getJMSReplyTo() != null) {
                Destination dest = message.getJMSReplyTo();
                map.put(JMSConstants.JMS_REPLY_TO,
                        dest instanceof Queue ?
                                ((Queue) dest).getQueueName() : ((Topic) dest).getTopicName());
            }
        } catch (JMSException ignore) {
        }

        // priority
        try {
            map.put(JMSConstants.JMS_TIMESTAMP, Long.toString(message.getJMSTimestamp()));
        } catch (JMSException ignore) {
        }

        // message type
        try {
            if (message.getJMSType() != null) {
                map.put(JMSConstants.JMS_TYPE, message.getJMSType());
            }
        } catch (JMSException ignore) {
        }

        // any other transport properties / headers
        Enumeration<?> e = null;
        try {
            e = message.getPropertyNames();
        } catch (JMSException ignore) {
        }

        if (e != null) {
            while (e.hasMoreElements()) {
                String headerName = (String) e.nextElement();
                try {
                    map.put(headerName, message.getStringProperty(headerName));
                    continue;
                } catch (JMSException ignore) {
                }
                try {
                    map.put(headerName, message.getBooleanProperty(headerName));
                    continue;
                } catch (JMSException ignore) {
                }
                try {
                    map.put(headerName, message.getIntProperty(headerName));
                    continue;
                } catch (JMSException ignore) {
                }
                try {
                    map.put(headerName, message.getLongProperty(headerName));
                    continue;
                } catch (JMSException ignore) {
                }
                try {
                    map.put(headerName, message.getDoubleProperty(headerName));
                    continue;
                } catch (JMSException ignore) {
                }
                try {
                    map.put(headerName, message.getFloatProperty(headerName));
                } catch (JMSException ignore) {
                }
            }
        }

        return map;
    }


    /**
     * Create a MessageConsumer for the given Destination
     *
     * @param session         JMS Session to use
     * @param dest            Destination for which the Consumer is to be created
     * @param messageSelector the message selector to be used if any
     * @return a MessageConsumer for the specified Destination
     * @throws javax.jms.JMSException
     */
    public static MessageConsumer createConsumer(Session session, Destination dest,
                                                 String messageSelector)
            throws JMSException {

        if (dest instanceof Queue) {
            return ((QueueSession) session).createReceiver((Queue) dest, messageSelector);
        } else {
            return ((TopicSession) session).createSubscriber((Topic) dest, messageSelector, false);
        }
    }

    /**
     * Create a temp queue or topic for synchronous receipt of responses, when a reply destination
     * is not specified
     *
     * @param session the JMS Session to use
     * @return a temporary Queue or Topic, depending on the session
     * @throws javax.jms.JMSException
     */
    public static Destination createTemporaryDestination(Session session) throws JMSException {

        if (session instanceof QueueSession) {
            return session.createTemporaryQueue();
        } else {
            return session.createTemporaryTopic();
        }
    }


    public static <T> T lookup(Context context, Class<T> clazz, String name)
            throws NamingException {

        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference) object;
                handleException("JNDI failed to de-reference Reference with name " +
                        name + "; is the factory " + ref.getFactoryClassName() +
                        " in your classpath?");
                return null;
            } else {
                handleException("JNDI lookup of name " + name + " returned a " +
                        object.getClass().getName() + " while a " + clazz + " was expected");
                return null;
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a Connection. Please be cautious when
     * making any changes
     *
     * @param conFac    the ConnectionFactory to use
     * @param user      optional user name
     * @param pass      optional password
     * @param jmsSpec11 should we use JMS 1.1 API ?
     * @param isQueue   is this to deal with a Queue?
     * @param isDurable whether the messaging provider is durable
     * @param clientID  durable subscriber client id
     * @return a JMS Connection as requested
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static Connection createConnection(ConnectionFactory conFac,
                                              String user, String pass, boolean jmsSpec11,
                                              Boolean isQueue,
                                              boolean isDurable, String clientID)
            throws JMSException {

        Connection connection = null;
        if (log.isDebugEnabled()) {
            log.debug("Creating a " + (isQueue ? "Queue" : "Topic") +
                    "Connection using credentials : (" + user + "/" + pass + ")");
        }

        if (jmsSpec11 || isQueue == null) {
            if (user != null && pass != null) {
                connection = conFac.createConnection(user, pass);
            } else {
                connection = conFac.createConnection();
            }
            if (isDurable) {
                connection.setClientID(clientID);
            }

        } else {
            QueueConnectionFactory qConFac = null;
            TopicConnectionFactory tConFac = null;
            if (isQueue) {
                qConFac = (QueueConnectionFactory) conFac;
            } else {
                tConFac = (TopicConnectionFactory) conFac;
            }

            if (user != null && pass != null) {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection(user, pass);
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection(user, pass);
                }
            } else {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection();
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection();
                }
            }
            if (isDurable) {
                connection.setClientID(clientID);
            }
        }
        return connection;
    }

    /**
     * This is a JMS spec independent method to create a Session. Please be cautious when
     * making any changes
     *
     * @param connection the JMS Connection
     * @param transacted should the session be transacted?
     * @param ackMode    the ACK mode for the session
     * @param jmsSpec11  should we use the JMS 1.1 API?
     * @param isQueue    is this Session to deal with a Queue?
     * @return a Session created for the given information
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static Session createSession(Connection connection, boolean transacted, int ackMode,
                                        boolean jmsSpec11, Boolean isQueue) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return connection.createSession(transacted, ackMode);

        } else {
            if (isQueue) {
                return ((QueueConnection) connection).createQueueSession(transacted, ackMode);
            } else {
                return ((TopicConnection) connection).createTopicSession(transacted, ackMode);
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a MessageConsumer. Please be cautious when
     * making any changes
     *
     * @param session         JMS session
     * @param destination     the Destination
     * @param isQueue         is the Destination a queue?
     * @param subscriberName  optional client name to use for a durable subscription to a topic
     * @param messageSelector optional message selector
     * @param pubSubNoLocal   should we receive messages sent by us during pub-sub?
     * @param isDurable       is this a durable topic subscription?
     * @param jmsSpec11       should we use JMS 1.1 API ?
     * @return a MessageConsumer to receive messages
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static MessageConsumer createConsumer(
            Session session, Destination destination, Boolean isQueue,
            String subscriberName, String messageSelector, boolean pubSubNoLocal,
            boolean isDurable, boolean jmsSpec11) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            if (isDurable) {
                return session.createDurableSubscriber(
                        (Topic) destination, subscriberName, messageSelector, pubSubNoLocal);
            } else {
                return session.createConsumer(destination, messageSelector, pubSubNoLocal);
            }
        } else {
            if (isQueue) {
                return ((QueueSession) session).createReceiver((Queue) destination, messageSelector);
            } else {
                if (isDurable) {
                    return ((TopicSession) session).createDurableSubscriber(
                            (Topic) destination, subscriberName, messageSelector, pubSubNoLocal);
                } else {
                    return ((TopicSession) session).createSubscriber(
                            (Topic) destination, messageSelector, pubSubNoLocal);
                }
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a MessageProducer. Please be cautious when
     * making any changes
     *
     * @param session     JMS session
     * @param destination the Destination
     * @param isQueue     is the Destination a queue?
     * @param jmsSpec11   should we use JMS 1.1 API ?
     * @return a MessageProducer to send messages to the given Destination
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static MessageProducer createProducer(
            Session session, Destination destination, Boolean isQueue, boolean jmsSpec11)
            throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return session.createProducer(destination);
        } else {
            if (isQueue) {
                return ((QueueSession) session).createSender((Queue) destination);
            } else {
                return ((TopicSession) session).createPublisher((Topic) destination);
            }
        }
    }

    /**
     * Return a String representation of the destination type
     *
     * @param destType the destination type indicator int
     * @return a descriptive String
     */
    public static String getDestinationTypeAsString(int destType) {
        if (destType == JMSConstants.QUEUE) {
            return "queue";
        } else if (destType == JMSConstants.TOPIC) {
            return "topic";
        } else {
            return "generic";
        }
    }

    /**
     * Return the JMS destination with the given destination name looked up from the context
     *
     * @param context         the Context to lookup
     * @param destinationName name of the destination to be looked up
     * @param destinationType type of the destination to be looked up
     * @return the JMS destination, or null if it does not exist
     */
    public static Destination lookupDestination(Context context, String destinationName,
                                                String destinationType) throws NamingException {

        if (destinationName == null) {
            return null;
        }

        try {
            return JMSUtils.lookup(context, Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            try {
                return JMSUtils.lookup(context, Destination.class,
                        (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType) ?
                                "dynamicTopics/" : "dynamicQueues/") + destinationName);
            } catch (NamingException x) {
                log.warn("Cannot locate destination : " + destinationName);
                throw x;
            }
        } catch (NamingException e) {
            log.warn("Cannot locate destination : " + destinationName, e);
            throw e;
        }
    }
}
