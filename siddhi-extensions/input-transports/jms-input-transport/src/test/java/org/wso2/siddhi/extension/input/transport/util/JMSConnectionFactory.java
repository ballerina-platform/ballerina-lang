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
package org.wso2.siddhi.extension.input.transport.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.extension.input.transport.exception.JMSInputAdaptorRuntimeException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Encapsulate a JMS Connection factory definition within an Axis2.xml.
 * <p/>
 * JMS Connection Factory definitions, allows JNDI properties as well as other service
 * level parameters to be defined, and re-used by each service that binds to it
 * <p/>
 * When used for sending messages out, the JMSConnectionFactory'ies are able to cache
 * a Connection, Session or Producer
 */
public class JMSConnectionFactory {

    private static final Logger log = Logger.getLogger(JMSConnectionFactory.class);

    /**
     * The list of parameters from the axis2.xml definition
     */
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    private String name;

    /**
     * The cached InitialContext reference
     */
    private Context context = null;
    /**
     * The JMS ConnectionFactory this definition refers to
     */
    private ConnectionFactory conFactory = null;
    /**
     * The shared JMS Connection for this JMS connection factory
     */
    private Connection sharedConnection = null;
    /**
     * The shared JMS Session for this JMS connection factory
     */
    private Session sharedSession = null;
    /**
     * The shared JMS MessageProducer for this JMS connection factory
     */
    private MessageProducer sharedProducer = null;
    /**
     * The Shared Destination
     */
    private Destination sharedDestination = null;
    /**
     * The shared JMS connection for this JMS connection factory
     */
    private int cacheLevel = JMSConstants.CACHE_CONNECTION;

    /**
     * Digest a JMS CF definition from an axis2.xml 'Parameter' and construct
     *
     * @param parameters the axis2.xml 'Parameter' that defined the JMS CF
     */
    public JMSConnectionFactory(Hashtable<String, String> parameters, String name) {
        this.parameters = parameters;
        this.name = name;

        digestCacheLevel();
        try {
            context = new InitialContext(parameters);
            conFactory = JMSUtils.lookup(context, ConnectionFactory.class,
                    parameters.get(JMSConstants.PARAM_CONFAC_JNDI_NAME));
            log.info("JMS ConnectionFactory : " + name + " initialized");

        } catch (NamingException e) {
            throw new JMSInputAdaptorRuntimeException("Cannot acquire JNDI context, JMS Connection factory : "
                    + parameters.get(JMSConstants.PARAM_CONFAC_JNDI_NAME)
                    + " or default destination : "
                    + parameters.get(JMSConstants.PARAM_DESTINATION) +
                    " for JMS CF : " + name + " using : " + parameters, e);
        }
    }

    /**
     * Digest, the cache value iff specified
     */
    private void digestCacheLevel() {

        String key = JMSConstants.PARAM_CACHE_LEVEL;
        String val = parameters.get(key);

        if ("none".equalsIgnoreCase(val)) {
            this.cacheLevel = JMSConstants.CACHE_NONE;
        } else if ("connection".equalsIgnoreCase(val)) {
            this.cacheLevel = JMSConstants.CACHE_CONNECTION;
        } else if ("session".equals(val)) {
            this.cacheLevel = JMSConstants.CACHE_SESSION;
        } else if ("producer".equals(val)) {
            this.cacheLevel = JMSConstants.CACHE_PRODUCER;
        } else if (val != null) {
            throw new JMSInputAdaptorRuntimeException("Invalid cache level : " + val + " for JMS CF : " + name);
        }
    }

    /**
     * Close all connections, sessions etc.. and stop this connection factory.
     */
    public synchronized void stop() {
        if (sharedConnection != null) {
            try {
                sharedConnection.close();
            } catch (JMSException e) {
                log.warn("Error shutting down connection factory : " + name, e);
            }
        }

        if (context != null) {
            try {
                context.close();
            } catch (NamingException e) {
                log.warn("Error while closing the InitialContext of factory : " + name, e);
            }
        }
    }

    /**
     * Return the name assigned to this JMS CF definition.
     *
     * @return name of the JMS CF
     */
    public String getName() {
        return name;
    }

    /**
     * The list of properties (including JNDI and non-JNDI)
     *
     * @return properties defined on the JMS CF
     */
    public Hashtable<String, String> getParameters() {
        return parameters;
    }

    /**
     * Get cached InitialContext
     *
     * @return cache InitialContext
     */
    public Context getContext() {
        return context;
    }

    /**
     * Cache level applicable for this JMS CF
     *
     * @return applicable cache level
     */
    public int getCacheLevel() {
        return cacheLevel;
    }

    /**
     * Get the shared Destination - if defined
     *
     * @return
     */
    public Destination getSharedDestination() {
        return sharedDestination;
    }

    /**
     * Lookup a Destination using this JMS CF definitions and JNDI name
     *
     * @param destinationName JNDI name of the Destionation
     * @return JMS Destination for the given JNDI name or null
     */
    public Destination getDestination(String destinationName) {
        try {
            return JMSUtils.lookupDestination(context, destinationName, parameters.get(JMSConstants.PARAM_DEST_TYPE));
        } catch (NamingException e) {
            handleException("Error looking up the JMS destination with name " + destinationName
                    + " of type " + parameters.get(JMSConstants.PARAM_DEST_TYPE), e);
        }

        // never executes but keeps the compiler happy
        return null;
    }

    /**
     * Get the reply Destination from the PARAM_REPLY_DESTINATION parameter
     *
     * @return reply destination defined in the JMS CF
     */
    public String getReplyToDestination() {
        return parameters.get(JMSConstants.PARAM_REPLY_DESTINATION);
    }

    /**
     * Get the reply destination type from the PARAM_REPLY_DEST_TYPE parameter
     *
     * @return reply destination defined in the JMS CF
     */
    public String getReplyDestinationType() {
        return parameters.get(JMSConstants.PARAM_REPLY_DEST_TYPE) != null ?
                parameters.get(JMSConstants.PARAM_REPLY_DEST_TYPE) :
                JMSConstants.DESTINATION_TYPE_GENERIC;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new JMSInputAdaptorRuntimeException(msg, e);
    }

    /**
     * Should the JMS 1.1 API be used? - defaults to yes
     *
     * @return true, if JMS 1.1 api should  be used
     */
    public boolean isJmsSpec11() {
        return parameters.get(JMSConstants.PARAM_JMS_SPEC_VER) == null ||
                "1.1".equals(parameters.get(JMSConstants.PARAM_JMS_SPEC_VER));
    }

    /**
     * Return the type of the JMS CF Destination
     *
     * @return TRUE if a Queue, FALSE for a Topic and NULL for a JMS 1.1 Generic Destination
     */
    public Boolean isQueue() {
        if (parameters.get(JMSConstants.PARAM_CONFAC_TYPE) == null &&
                parameters.get(JMSConstants.PARAM_DEST_TYPE) == null) {
            return null;
        }

        if (parameters.get(JMSConstants.PARAM_CONFAC_TYPE) != null) {
            if ("queue".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_CONFAC_TYPE))) {
                return true;
            } else if ("topic".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_CONFAC_TYPE))) {
                return false;
            } else {
                throw new JMSInputAdaptorRuntimeException("Invalid " + JMSConstants.PARAM_CONFAC_TYPE + " : " +
                        parameters.get(JMSConstants.PARAM_CONFAC_TYPE)
                        + " for JMS CF : " + name);
            }
        } else {
            if ("queue".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DEST_TYPE))) {
                return true;
            } else if ("topic".equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DEST_TYPE))) {
                return false;
            } else {
                throw new JMSInputAdaptorRuntimeException("Invalid " + JMSConstants.PARAM_DEST_TYPE + " : " +
                        parameters.get(JMSConstants.PARAM_DEST_TYPE)
                        + " for JMS CF : " + name);
            }
        }
    }

    /**
     * Is a session transaction requested from users of this JMS CF?
     *
     * @return session transaction required by the clients of this?
     */
    private boolean isSessionTransacted() {
        return parameters.get(JMSConstants.PARAM_SESSION_TRANSACTED) != null &&
                Boolean.valueOf(parameters.get(JMSConstants.PARAM_SESSION_TRANSACTED));
    }

    private boolean isDurable() {
        if (parameters.get(JMSConstants.PARAM_SUB_DURABLE) != null) {
            return Boolean.valueOf(parameters.get(JMSConstants.PARAM_SUB_DURABLE));
        }
        return false;
    }

    private String getClientId() {
        return parameters.get(JMSConstants.PARAM_DURABLE_SUB_CLIENT_ID);
    }

    /**
     * Create a new Connection
     *
     * @return a new Connection
     */
    private Connection createConnection() {

        Connection connection = null;
        try {
            connection = JMSUtils.createConnection(
                    conFactory,
                    parameters.get(JMSConstants.PARAM_JMS_USERNAME),
                    parameters.get(JMSConstants.PARAM_JMS_PASSWORD),
                    isJmsSpec11(), isQueue(), isDurable(), getClientId());

            if (log.isDebugEnabled()) {
                log.debug("New JMS Connection from JMS CF : " + name + " created");
            }

        } catch (JMSException e) {
            handleException("Error acquiring a Connection from the JMS CF : " + name +
                    " using properties : " + parameters, e);
        }
        return connection;
    }

    /**
     * Create a new Session
     *
     * @param connection Connection to use
     * @return A new Session
     */
    private Session createSession(Connection connection) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a new JMS Session from JMS CF : " + name);
            }
            return JMSUtils.createSession(
                    connection, isSessionTransacted(), Session.AUTO_ACKNOWLEDGE, isJmsSpec11(), isQueue());

        } catch (JMSException e) {
            handleException("Error creating JMS session from JMS CF : " + name, e);
        }
        return null;
    }

    /**
     * Create a new MessageProducer
     *
     * @param session     Session to be used
     * @param destination Destination to be used
     * @return a new MessageProducer
     */
    private MessageProducer createProducer(Session session, Destination destination) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a new JMS MessageProducer from JMS CF : " + name);
            }

            return JMSUtils.createProducer(
                    session, destination, isQueue(), isJmsSpec11());

        } catch (JMSException e) {
            handleException("Error creating JMS producer from JMS CF : " + name, e);
        }
        return null;
    }

    /**
     * Get a new Connection or shared Connection from this JMS CF
     *
     * @return new or shared Connection from this JMS CF
     */
    public Connection getConnection() {
        if (cacheLevel > JMSConstants.CACHE_NONE) {
            return getSharedConnection();
        } else {
            return createConnection();
        }
    }

    /**
     * Get a new Session or shared Session from this JMS CF
     *
     * @param connection the Connection to be used
     * @return new or shared Session from this JMS CF
     */
    public Session getSession(Connection connection) {
        if (cacheLevel > JMSConstants.CACHE_CONNECTION) {
            return getSharedSession();
        } else {
            return createSession((connection == null ? getConnection() : connection));
        }
    }

    /**
     * Get a new MessageProducer or shared MessageProducer from this JMS CF
     *
     * @param connection  the Connection to be used
     * @param session     the Session to be used
     * @param destination the Destination to bind MessageProducer to
     * @return new or shared MessageProducer from this JMS CF
     */
    public MessageProducer getMessageProducer(
            Connection connection, Session session, Destination destination) {
        if (cacheLevel > JMSConstants.CACHE_SESSION) {
            return getSharedProducer();
        } else {
            return createProducer((session == null ? getSession(connection) : session), destination);
        }
    }

    /**
     * Get a new Connection or shared Connection from this JMS CF
     *
     * @return new or shared Connection from this JMS CF
     */
    private synchronized Connection getSharedConnection() {
        if (sharedConnection == null) {
            sharedConnection = createConnection();
            if (log.isDebugEnabled()) {
                log.debug("Created shared JMS Connection for JMS CF : " + name);
            }
        }
        return sharedConnection;
    }

    /**
     * Get a shared Session from this JMS CF
     *
     * @return shared Session from this JMS CF
     */
    private synchronized Session getSharedSession() {
        if (sharedSession == null) {
            sharedSession = createSession(getSharedConnection());
            if (log.isDebugEnabled()) {
                log.debug("Created shared JMS Session for JMS CF : " + name);
            }
        }
        return sharedSession;
    }

    /**
     * Get a shared MessageProducer from this JMS CF
     *
     * @return shared MessageProducer from this JMS CF
     */
    private synchronized MessageProducer getSharedProducer() {
        if (sharedProducer == null) {
            sharedProducer = createProducer(getSharedSession(), sharedDestination);
            if (log.isDebugEnabled()) {
                log.debug("Created shared JMS MessageConsumer for JMS CF : " + name);
            }
        }
        return sharedProducer;
    }
}
