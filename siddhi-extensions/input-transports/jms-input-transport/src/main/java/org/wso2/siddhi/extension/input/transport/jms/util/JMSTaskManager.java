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

import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.log4j.Logger;
import org.wso2.siddhi.extension.input.transport.jms.exception.JMSInputAdaptorRuntimeException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Each adapter will have one JMSTaskManager instance that will create, manage and also destroy
 * idle tasks created for it, for message receipt. This will also allow individual tasks to cache
 * the Connection, Session or Consumer as necessary, considering the transactionality required and
 * user preference.
 * <p/>
 * This also acts as the ExceptionListener for all JMS connections made on behalf of the adapter.
 * Since the ExceptionListener is notified by a JMS provider on a "serious" error, we simply try
 * to re-connect. Thus a connection failure for a single task, will re-initialize the state afresh
 * for the transport adapter, by discarding all connections.
 */
public class JMSTaskManager {

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(JMSTaskManager.class);

    /**
     * The Task manager is stopped or has not started
     */
    private static final int STATE_STOPPED = 0;
    /**
     * The Task manager is started and active
     */
    private static final int STATE_STARTED = 1;
    /**
     * The Task manager is paused temporarily
     */
    private static final int STATE_PAUSED = 2;
    /**
     * The Task manager is started, but a shutdown has been requested
     */
    private static final int STATE_SHUTTING_DOWN = 3;
    /**
     * The Task manager has encountered an error
     */
    private static final int STATE_FAILURE = 4;

    /**
     * The name of the adapter managed by this instance
     */
    private String eventAdapterName;
    /**
     * The ConnectionFactory MUST refer to an XAConnectionFactory to use JTA
     */
    private String connFactoryJNDIName;
    /**
     * The JNDI name of the Destination Queue or Topic
     */
    // TODO: this overlaps with JMSEndpoint#jndiDestinationName; needs to be clarified
    private String destinationJNDIName;
    /**
     * JNDI location for the JTA UserTransaction
     */
    private String userTransactionJNDIName = "java:comp/UserTransaction";
    /**
     * The type of destination - P2P or PubSub (or JMS 1.1 API generic?)
     */
    // TODO: this overlaps with JMSEndpoint#destinationType; needs to be clarified
    private int destinationType = JMSConstants.GENERIC;
    /**
     * An optional message selector
     */
    private String messageSelector = null;

    /**
     * Should tasks run without transactions, using transacted Sessions (i.e. local), or JTA
     */
    private int transactionality = BaseConstants.TRANSACTION_NONE;
    /**
     * Should created Sessions be transactional ? - should be false when using JTA
     */
    private boolean sessionTransacted = true;
    /**
     * Session acknowledgement mode when transacted Sessions (i.e. local transactions) are used
     */
    private int sessionAckMode = Session.AUTO_ACKNOWLEDGE;

    /**
     * Is the subscription durable ?
     */
    private boolean subscriptionDurable = false;
    /**
     * The name of the durable subscriber for this client
     */
    private String durableSubscriberName = null;
    /**
     * In PubSub mode, should I receive messages sent by me / my connection ?
     */
    private boolean pubSubNoLocal = false;
    /**
     * Number of concurrent consumers - for PubSub, this should be 1 to prevent multiple receipt
     */
    private int concurrentConsumers = 1;
    /**
     * Maximum number of consumers to create - see @concurrentConsumers
     */
    private int maxConcurrentConsumers = 1;
    /**
     * The number of idle (i.e. message-less) attempts to be tried before suicide, to scale down
     */
    private int idleTaskExecutionLimit = 10;
    /**
     * The maximum number of successful message receipts for a task - to limit thread life span
     */
    private int maxMessagesPerTask = -1;    // default is unlimited
    /**
     * The default receive timeout - a negative value means wait forever, zero dont wait at all
     */
    private int receiveTimeout = 1000;
    /**
     * JMS Resource cache level - Connection, Session, Consumer. Auto will select safe default
     */
    private int cacheLevel = JMSConstants.CACHE_AUTO;
    /**
     * Should we cache the UserTransaction handle from JNDI - true for almost all app servers
     */
    private boolean cacheUserTransaction = true;
    /**
     * Shared UserTransactionHandle
     */
    private UserTransaction sharedUserTransaction = null;
    /**
     * Should this transport adapter use JMS 1.1 ? (when false, defaults to 1.0.2b)
     */
    private boolean jmsSpec11 = false;

    /**
     * Initial duration to attempt re-connection to JMS provider after failure
     */
    private int initialReconnectDuration = 10000;
    /**
     * Progression factory for geometric series that calculates re-connection times
     */
    private double reconnectionProgressionFactor = 2.0; // default to [bounded] exponential
    /**
     * Upper limit on reconnection attempt duration
     */
    private long maxReconnectDuration = 1000 * 60 * 60; // 1 hour

    /**
     * The JNDI context properties and other general properties
     */
    private Hashtable<String, String> jmsProperties = new Hashtable<String, String>();
    /**
     * The JNDI Context acuired
     */
    private Context context = null;
    /**
     * The ConnectionFactory to be used
     */
    private ConnectionFactory conFactory = null;
    /**
     * The JMS Destination
     */
    private Destination destination = null;

    /**
     * The list of active tasks thats managed by this instance
     */
    private final List<MessageListenerTask> pollingTasks =
            Collections.synchronizedList(new ArrayList<MessageListenerTask>());
    /**
     * The per-service JMS message receiver to be invoked after receipt of messages
     */
    private JMSMessageListener jmsMessageListener = null;

    /**
     * State of this Task Manager
     */
    private volatile int JMSTaskManagerState = STATE_STOPPED;
    /**
     * Number of invoker tasks active
     */
    private volatile int activeTaskCount = 0;
    /**
     * The number of existing JMS message consumers.
     */
    private final AtomicInteger consumerCount = new AtomicInteger();
    /**
     * The shared thread pool from the Listener
     */
    private WorkerPool workerPool = null;

    /**
     * The JMS Connection shared between multiple polling tasks - when enabled (reccomended)
     */
    private Connection sharedConnection = null;

    private String durableSubscriberClientId = null;

    private volatile boolean isOnExceptionError = false;

    public void setDurableSubscriberClientId(String durableSubscriberClientId) {
        this.durableSubscriberClientId = durableSubscriberClientId;
    }

    /**
     * Start or re-start the Task Manager by shutting down any existing worker tasks and
     * re-creating them. However, if this is STM is PAUSED, a start request is ignored.
     * This applies for any connection failures during paused state as well, which then will
     * not try to auto recover
     */
    public synchronized void start() {

        if (JMSTaskManagerState == STATE_PAUSED) {
            log.info("Attempt to re-start paused TaskManager is ignored. Please use resume instead");
            return;
        }

        // if any tasks are running, stop whats running now
        if (!pollingTasks.isEmpty()) {
            stop();
        }

        if (cacheLevel == JMSConstants.CACHE_AUTO) {
            cacheLevel =
                    transactionality == BaseConstants.TRANSACTION_NONE ?
                            JMSConstants.CACHE_CONSUMER : JMSConstants.CACHE_NONE;
        }
        switch (cacheLevel) {
            case JMSConstants.CACHE_NONE:
                log.debug("No JMS resources will be cached/shared between poller " +
                        "worker tasks of event adapter : " + eventAdapterName);
                break;
            case JMSConstants.CACHE_CONNECTION:
                log.debug("Only the JMS Connection will be cached and shared between *all* " +
                        "poller task invocations");
                break;
            case JMSConstants.CACHE_SESSION:
                log.debug("The JMS Connection and Session will be cached and shared between " +
                        "successive poller task invocations");
                break;
            case JMSConstants.CACHE_CONSUMER:
                log.debug("The JMS Connection, Session and MessageConsumer will be cached and " +
                        "shared between successive poller task invocations");
                break;
            default: {
                handleException("Invalid cache level : " + cacheLevel +
                        " for event adapter : " + eventAdapterName);
            }
        }

        for (int i = 0; i < concurrentConsumers; i++) {
            workerPool.execute(new MessageListenerTask());
        }

        JMSTaskManagerState = STATE_STARTED;
        log.info("Task manager for event adapter : " + eventAdapterName + " [re-]initialized");
    }

    /**
     * Shutdown the tasks and release any shared resources
     */
    public synchronized void stop() {

        if (log.isDebugEnabled()) {
            log.debug("Stopping JMSTaskManager for event adapter : " + eventAdapterName);
        }

        if (JMSTaskManagerState != STATE_FAILURE) {
            JMSTaskManagerState = STATE_SHUTTING_DOWN;
        }

        synchronized (pollingTasks) {
            for (MessageListenerTask lstTask : pollingTasks) {
                lstTask.requestShutdown();
            }
        }

        // try to wait a bit for task shutdown
        for (int i = 0; i < 5; i++) {
            if (activeTaskCount == 0) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }

        if (sharedConnection != null) {
            try {
                sharedConnection.close();
            } catch (JMSException e) {
                logError("Error closing shared Connection", e);
            } finally {
                sharedConnection = null;
            }
        }

        if (activeTaskCount > 0) {
            log.warn("Unable to shutdown all polling tasks of event adapter : " + eventAdapterName);
        }

        if (JMSTaskManagerState != STATE_FAILURE) {
            JMSTaskManagerState = STATE_STOPPED;
        }
        log.info("Task manager for event adapter : " + receiveTimeout + " shutdown");
    }

    /**
     * Temporarily suspend receipt and processing of messages. Accomplished by stopping the
     * connection / or connections used by the poller tasks
     */
    public synchronized void pause() {
        for (MessageListenerTask lstTask : pollingTasks) {
            lstTask.pause();
        }
        if (sharedConnection != null) {
            try {
                sharedConnection.stop();
            } catch (JMSException e) {
                logError("Error pausing shared Connection", e);
            }
        }
    }

    /**
     * Resume receipt and processing of messages of paused tasks
     */
    public synchronized void resume() {
        for (MessageListenerTask lstTask : pollingTasks) {
            lstTask.resume();
        }
        if (sharedConnection != null) {
            try {
                sharedConnection.start();
            } catch (JMSException e) {
                logError("Error resuming shared Connection", e);
            }
        }
    }

    /**
     * Start a new MessageListenerTask if we are still active, the threshold is not reached, and w
     * e do not have any idle tasks - i.e. scale up listening
     */
    private void scheduleNewTaskIfAppropriate() {
        if (JMSTaskManagerState == STATE_STARTED &&
                pollingTasks.size() < getMaxConcurrentConsumers() && getIdleTaskCount() == 0) {
            workerPool.execute(new MessageListenerTask());
        }
    }

    /**
     * Get the number of MessageListenerTasks that are currently idle
     *
     * @return idle task count
     */
    private int getIdleTaskCount() {
        int count = 0;
        for (MessageListenerTask lstTask : pollingTasks) {
            if (lstTask.isTaskIdle()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of MessageListenerTasks that are currently connected to the JMS provider
     *
     * @return connected task count
     */
    private int getConnectedTaskCount() {
        int count = 0;
        for (MessageListenerTask lstTask : pollingTasks) {
            if (lstTask.isConnected()) {
                count++;
            }
        }
        return count;
    }

    /**
     * The actual threads/tasks that perform message polling
     */
    private class MessageListenerTask implements Runnable, ExceptionListener {

        /**
         * The Connection used by the polling task
         */
        private Connection connection = null;
        /**
         * The Sesson used by the polling task
         */
        private Session session = null;
        /**
         * The MessageConsumer used by the polling task
         */
        private MessageConsumer consumer = null;
        /**
         * State of the worker polling task
         */
        private volatile int workerState = STATE_STOPPED;
        /**
         * The number of idle (i.e. without fetching a message) polls for this task
         */
        private int idleExecutionCount = 0;
        /**
         * Is this task idle right now?
         */
        private volatile boolean idle = false;
        /**
         * Is this task connected to the JMS provider successfully?
         */
        private volatile boolean connected = false;

        /**
         * As soon as we create a new polling task, add it to the STM for control later
         */
        MessageListenerTask() {
            synchronized (pollingTasks) {
                pollingTasks.add(this);
            }
        }

        /**
         * Pause this polling worker task
         */
        public void pause() {
            if (isActive()) {
                if (connection != null && cacheLevel < JMSConstants.CACHE_CONNECTION) {
                    try {
                        connection.stop();
                    } catch (JMSException e) {
                        log.warn("Error pausing Message Listener task for event adapter : " + eventAdapterName);
                    }
                }
                workerState = STATE_PAUSED;
            }
        }

        /**
         * Resume this polling task
         */
        public void resume() {
            if (connection != null && cacheLevel < JMSConstants.CACHE_CONNECTION) {
                try {
                    connection.start();
                } catch (JMSException e) {
                    log.warn("Error resuming Message Listener task for event adapter : " + eventAdapterName);
                }
            }
            workerState = STATE_STARTED;
        }

        /**
         * Execute the polling worker task
         */
        public void run() {
            workerState = STATE_STARTED;
            activeTaskCount++;
            int messageCount = 0;

            if (log.isDebugEnabled()) {
                log.debug("New poll task starting : thread id = " + Thread.currentThread().getId());
            }

            try {
                while (isActive() &&
                        (getMaxMessagesPerTask() < 0 || messageCount < getMaxMessagesPerTask()) &&
                        (getConcurrentConsumers() == 1 || idleExecutionCount < getIdleTaskExecutionLimit())) {

                    UserTransaction ut = null;
                    try {
                        if (transactionality == BaseConstants.TRANSACTION_JTA) {
                            ut = getUserTransaction();
                            // We will only create a new tx if there is no tx alive 
                            if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
                                ut.begin();
                            }
                        }
                    } catch (NotSupportedException e) {
                        handleException("Listener Task is already associated with a transaction", e);
                    } catch (SystemException e) {
                        handleException("Error starting a JTA transaction", e);
                    }

                    // Get a message by polling, or receive null
                    Message message = receiveMessage();

                    if (log.isTraceEnabled()) {
                        if (message != null) {
                            try {
                                log.trace("<<<<<<< READ message with Message ID : " +
                                        message.getJMSMessageID() + " from : " + destination +
                                        " by Thread ID : " + Thread.currentThread().getId());
                            } catch (JMSException ignore) {
                            }
                        } else {
                            log.trace("No message received by Thread ID : " +
                                    Thread.currentThread().getId() + " for destination : " + destination);
                        }
                    }

                    if (message != null) {
                        idle = false;
                        idleExecutionCount = 0;
                        messageCount++;
                        // I will be busy now while processing this message, so start another if needed
                        scheduleNewTaskIfAppropriate();
                        handleMessage(message, ut);

                    } else {
                        idle = true;
                        idleExecutionCount++;
                    }
                }

            } finally {

                if (log.isTraceEnabled()) {
                    log.trace("Listener task with Thread ID : " + Thread.currentThread().getId() +
                            " is stopping after processing : " + messageCount + " messages :: " +
                            " isActive : " + isActive() + " maxMessagesPerTask : " +
                            getMaxMessagesPerTask() + " concurrentConsumers : " + getConcurrentConsumers() +
                            " idleExecutionCount : " + idleExecutionCount + " idleTaskExecutionLimit : " +
                            getIdleTaskExecutionLimit());
                } else if (log.isDebugEnabled()) {
                    log.debug("Listener task with Thread ID : " + Thread.currentThread().getId() +
                            " is stopping after processing : " + messageCount + " messages");
                }

                // Close the consumer and session before decrementing activeTaskCount.
                // (If we have a shared connection, Qpid deadlocks if the shared connection
                //  is closed on another thread while closing the session)
                closeConsumer(true);
                closeSession(true);
                closeConnection();

                workerState = STATE_STOPPED;
                activeTaskCount--;
                synchronized (pollingTasks) {
                    pollingTasks.remove(this);
                }

                // if this is a JMS onException, ServiceTaskManager#onException will schedule
                // a new polling task
                if (!isOnExceptionError) {
                    // My time is up, so if I am going away, create another
                    scheduleNewTaskIfAppropriate();
                }
            }

        }

        /**
         * Poll for and return a message if available
         *
         * @return a message read, or null
         */
        private Message receiveMessage() {

            // get a new connection, session and consumer to prevent a conflict.
            // If idle, it means we can re-use what we already have 
            if (consumer == null) {
                connection = getConnection();
                session = getSession();
                consumer = getMessageConsumer();
                if (log.isDebugEnabled()) {
                    log.debug("Preparing a Connection, Session and Consumer to read messages");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Waiting for a message for event adapter : " + eventAdapterName + " - duration : "
                        + (getReceiveTimeout() < 0 ? "unlimited" : (getReceiveTimeout() + "ms")));
            }

            try {
                if (getReceiveTimeout() < 0) {
                    return consumer.receive();
                } else {
                    return consumer.receive(getReceiveTimeout());
                }
            } catch (IllegalStateException ignore) {
                // probably the consumer (shared) was closed.. which is still ok.. as we didn't read
            } catch (JMSException e) {
                logError("Error receiving message for event adapter : " + eventAdapterName, e);
            }
            return null;
        }

        /**
         * Invoke ultimate message handler/listener and ack message and/or
         * commit/rollback transactions
         *
         * @param message the JMS message received
         * @param ut      the UserTransaction used to receive this message, or null
         */
        private void handleMessage(Message message, UserTransaction ut) {

            String messageId = null;
            try {
                messageId = message.getJMSMessageID();
            } catch (JMSException ignore) {
            }

            boolean commitOrAck = true;
            try {
                jmsMessageListener.onMessage(message);
            } finally {

                // if client acknowledgement is selected, and processing requested ACK
                if (commitOrAck && getSessionAckMode() == Session.CLIENT_ACKNOWLEDGE) {
                    try {
                        message.acknowledge();
                        if (log.isDebugEnabled()) {
                            log.debug("Message : " + messageId + " acknowledged");
                        }
                    } catch (JMSException e) {
                        logError("Error acknowledging message : " + messageId, e);
                    }
                }

                // if session was transacted, commit it or rollback
                try {
                    if (session.getTransacted()) {
                        if (commitOrAck) {
                            session.commit();
                            if (log.isDebugEnabled()) {
                                log.debug("Session for message : " + messageId + " committed");
                            }
                        } else {
                            session.rollback();
                            if (log.isDebugEnabled()) {
                                log.debug("Session for message : " + messageId + " rolled back");
                            }
                        }
                    }
                } catch (JMSException e) {
                    logError("Error " + (commitOrAck ? "committing" : "rolling back") +
                            " local session txn for message : " + messageId, e);
                }

                // if a JTA transaction was being used, commit it or rollback
                try {
                    if (ut != null) {
                        if (commitOrAck) {
                            ut.commit();
                            if (log.isDebugEnabled()) {
                                log.debug("JTA txn for message : " + messageId + " committed");
                            }
                        } else {
                            ut.rollback();
                            if (log.isDebugEnabled()) {
                                log.debug("JTA txn for message : " + messageId + " rolled back");
                            }
                        }
                    }
                } catch (Exception e) {
                    logError("Error " + (commitOrAck ? "committing" : "rolling back") +
                            " JTA txn for message : " + messageId + " from the session", e);
                }

                // close the consumer
                closeConsumer(false);

                closeSession(false);
                closeConnection();
            }
        }

        /**
         * Handle JMS Connection exceptions by re-initializing. A single connection failure could
         * cause re-initialization of multiple MessageListenerTasks / Connections
         */
        public void onException(JMSException j) {

            isOnExceptionError = true;

            if (!isSTMActive()) {
                requestShutdown();
                return;
            }

            setConnected(false);

            if (cacheLevel < JMSConstants.CACHE_CONNECTION) {
                // failed Connection was not shared, thus no need to restart the whole STM
                log.warn("JMS Connection failure : " + j.getMessage());
                requestShutdown();
                return;
            }

            // if we failed while active, update state to show failure
            setJMSTaskManagerState(STATE_FAILURE);
            log.error("JMS Connection failed : " + j.getMessage() + " - shutting down worker tasks");

            int r = 1;

            long retryDuration = initialReconnectDuration;

            do {
                try {
                    log.info("Reconnection attempt : " + r + " for event adapter : " + eventAdapterName);
                    start();
                } catch (Exception ignore) {
                }

                boolean connected = false;
                for (int i = 0; i < 5; i++) {
                    if (getConnectedTaskCount() == concurrentConsumers) {
                        connected = true;
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }

                if (!connected) {
                    retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                    log.error("Reconnection attempt : " + (r++) + " for event adapter : " + eventAdapterName +
                            " failed. Next retry in " + (retryDuration / 1000) + " seconds");
                    if (retryDuration > maxReconnectDuration) {
                        retryDuration = maxReconnectDuration;
                    }

                    try {
                        Thread.sleep(retryDuration);
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    isOnExceptionError = false;
                    log.info("Reconnection attempt: " + r + " for event adapter: " + eventAdapterName +
                            " was successful!");
                }


            } while (!isSTMActive() || getConnectedTaskCount() < concurrentConsumers);
        }

        protected void requestShutdown() {
            workerState = STATE_SHUTTING_DOWN;
        }

        private boolean isActive() {
            return workerState == STATE_STARTED;
        }

        protected boolean isTaskIdle() {
            return idle;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        /**
         * Get a Connection that could/should be used by this task - depends on the cache level to reuse
         *
         * @return the shared Connection if cache level is higher than CACHE_NONE, or a new Connection
         */
        private Connection getConnection() {
            if (cacheLevel < JMSConstants.CACHE_CONNECTION) {
                // Connection is not shared
                if (connection == null) {
                    connection = createConnection();
                    setConnected(true);
                }

            } else if (connection == null) {
                // Connection is shared, but may not have been created

                synchronized (JMSTaskManager.this) {
                    if (sharedConnection == null) {
                        sharedConnection = createConnection();
                    }
                }
                connection = sharedConnection;
                setConnected(true);

            }
            // else: Connection is shared and is already referenced by this.connection

            return connection;
        }

        /**
         * Get a Session that could/should be used by this task - depends on the cache level to reuse
         *
         * @return the shared Session if cache level is higher than CACHE_CONNECTION, or a new Session
         * created using the Connection passed, or a new/shared connection
         */
        private Session getSession() {
            if (session == null || cacheLevel < JMSConstants.CACHE_SESSION) {
                session = createSession();
            }
            return session;
        }

        /**
         * Get a MessageConsumer that chould/should be used by this task - depends on the cache
         * level to reuse
         *
         * @return the shared MessageConsumer if cache level is higher than CACHE_SESSION, or a new
         * MessageConsumer possibly using the Connection and Session passed in
         */
        private MessageConsumer getMessageConsumer() {
            if (consumer == null || cacheLevel < JMSConstants.CACHE_CONSUMER) {
                consumer = createConsumer();
            }
            return consumer;
        }

        /**
         * Close the given Connection, hiding exceptions if any which are logged
         */
        private void closeConnection() {
            if (connection != null &&
                    cacheLevel < JMSConstants.CACHE_CONNECTION) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing non-shared JMS connection for event adapter : " + eventAdapterName);
                    }
                    connection.close();
                } catch (JMSException e) {
                    logError("Error closing JMS connection", e);
                } finally {
                    connection = null;
                }
            }
        }

        /**
         * Close the given Session, hiding exceptions if any which are logged
         *
         * @param forced the Session to be closed
         */
        private void closeSession(boolean forced) {
            if (session != null &&
                    (cacheLevel < JMSConstants.CACHE_SESSION || forced)) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing non-shared JMS session for event adapter : " + eventAdapterName);
                    }
                    session.close();
                } catch (JMSException e) {
                    logError("Error closing JMS session", e);
                } finally {
                    session = null;
                }
            }
        }

        /**
         * Close the given Consumer, hiding exceptions if any which are logged
         *
         * @param forced the Consumer to be closed
         */
        private void closeConsumer(boolean forced) {
            if (consumer != null &&
                    (cacheLevel < JMSConstants.CACHE_CONSUMER || forced)) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Closing non-shared JMS consumer for event adapter : " + eventAdapterName);
                    }
                    consumerCount.decrementAndGet();
                    consumer.close();
                } catch (JMSException e) {
                    logError("Error closing JMS consumer", e);
                } finally {
                    consumer = null;
                }
            }
        }

        /**
         * Create a new Connection for this STM, using JNDI properties and credentials provided
         *
         * @return a new Connection for this STM, using JNDI properties and credentials provided
         */
        private Connection createConnection() {

            try {
                conFactory = JMSUtils.lookup(
                        getInitialContext(), ConnectionFactory.class, getConnFactoryJNDIName());
                log.debug("Connected to the JMS connection factory : " + getConnFactoryJNDIName());
            } catch (NamingException e) {
                handleException("Error looking up connection factory : " + getConnFactoryJNDIName() +
                        " using JNDI properties : " + jmsProperties, e);
            }

            Connection connection = null;
            try {
                connection = JMSUtils.createConnection(
                        conFactory,
                        jmsProperties.get(JMSConstants.PARAM_JMS_USERNAME),
                        jmsProperties.get(JMSConstants.PARAM_JMS_PASSWORD),
                        isJmsSpec11(), isQueue(), isSubscriptionDurable(), durableSubscriberClientId);

                connection.setExceptionListener(this);
                connection.start();
                log.debug("JMS Connection for event adapter : " + eventAdapterName + " created and started");

            } catch (JMSException e) {
                if (connection != null) {
                    log.warn("Closing connection due to error:" + e.getMessage());
                    try {
                        connection.close();
                    } catch (JMSException ex) {
                        log.error("Error when cleaning up connection:" + ex.getMessage(), e);
                    }
                }
                handleException("Error acquiring a JMS connection to : " + getConnFactoryJNDIName() +
                        " using JNDI properties : " + jmsProperties, e);
            }
            return connection;
        }

        /**
         * Create a new Session for this STM
         *
         * @return a new Session created using the Connection passed in
         */
        private Session createSession() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Creating a new JMS Session for event adapter : " + eventAdapterName);
                }
                return JMSUtils.createSession(
                        connection, isSessionTransacted(), getSessionAckMode(), isJmsSpec11(), isQueue());

            } catch (JMSException e) {
                handleException("Error creating JMS session for event adapter : " + eventAdapterName, e);
            }
            return null;
        }

        /**
         * Create a new MessageConsumer for this STM
         *
         * @return a new MessageConsumer created using the Session passed in
         */
        private MessageConsumer createConsumer() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Creating a new JMS MessageConsumer for event adapter : " + eventAdapterName);
                }

                MessageConsumer consumer = JMSUtils.createConsumer(
                        session, getDestination(session), isQueue(),
                        (isSubscriptionDurable() && getDurableSubscriberName() != null ?
                                getDurableSubscriberName() : eventAdapterName),
                        getMessageSelector(), isPubSubNoLocal(), isSubscriptionDurable(), isJmsSpec11());
                consumerCount.incrementAndGet();
                return consumer;

            } catch (JMSException e) {
                handleException("Error creating JMS consumer for event adapter : " + eventAdapterName, e);
            }
            return null;
        }
    }

    // -------------- mundane private methods ----------------

    /**
     * Get the InitialContext for lookup using the JNDI parameters applicable to the event adapter
     *
     * @return the InitialContext to be used
     * @throws NamingException
     */
    private Context getInitialContext() throws NamingException {
        if (context == null) {
            context = new InitialContext(jmsProperties);
        }
        return context;
    }

    /**
     * Return the JMS Destination for the JNDI name of the Destination from the InitialContext
     *
     * @param session which is used to create the destinations if not present and if possible
     * @return the JMS Destination to which this STM listens for messages
     */
    private Destination getDestination(Session session) {
        if (destination == null) {
            try {
                context = getInitialContext();
                destination = JMSUtils.lookupDestination(context, getDestinationJNDIName(),
                        JMSUtils.getDestinationTypeAsString(destinationType));
                if (log.isDebugEnabled()) {
                    log.debug("JMS Destination with JNDI name : " + getDestinationJNDIName() +
                            " found for event adapter " + eventAdapterName);
                }
            } catch (NamingException e) {
                try {
                    switch (destinationType) {
                        case JMSConstants.QUEUE: {
                            destination = session.createQueue(getDestinationJNDIName());
                            break;
                        }
                        case JMSConstants.TOPIC: {
                            destination = session.createTopic(getDestinationJNDIName());
                            break;
                        }
                        default: {
                            handleException("Error looking up JMS destination : " +
                                    getDestinationJNDIName() + " using JNDI properties : " +
                                    jmsProperties, e);
                        }
                    }
                } catch (JMSException j) {
                    handleException("Error looking up JMS destination and auto " +
                            "creating JMS destination : " + getDestinationJNDIName() +
                            " using JNDI properties : " + jmsProperties, e);
                }
            }
        }
        return destination;
    }

    /**
     * The UserTransaction to be used, looked up from the JNDI
     *
     * @return The UserTransaction to be used, looked up from the JNDI
     */
    private UserTransaction getUserTransaction() {
        if (!cacheUserTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Acquiring a new UserTransaction for event adapter : " + eventAdapterName);
            }

            try {
                context = getInitialContext();
                return
                        JMSUtils.lookup(context, UserTransaction.class, getUserTransactionJNDIName());
            } catch (NamingException e) {
                handleException("Error looking up UserTransaction : " + getUserTransactionJNDIName() +
                        " using JNDI properties : " + jmsProperties, e);
            }
        }

        if (sharedUserTransaction == null) {
            try {
                context = getInitialContext();
                sharedUserTransaction =
                        JMSUtils.lookup(context, UserTransaction.class, getUserTransactionJNDIName());
                if (log.isDebugEnabled()) {
                    log.debug("Acquired shared UserTransaction for event adator : " + eventAdapterName);
                }
            } catch (NamingException e) {
                handleException("Error looking up UserTransaction : " + getUserTransactionJNDIName() +
                        " using JNDI properties : " + jmsProperties, e);
            }
        }
        return sharedUserTransaction;
    }

    // -------------------- trivial methods ---------------------
    private boolean isSTMActive() {
        return JMSTaskManagerState == STATE_STARTED;
    }

    /**
     * Is this STM bound to a Queue, Topic or a JMS 1.1 Generic Destination?
     *
     * @return TRUE for a Queue, FALSE for a Topic and NULL for a Generic Destination
     */
    public Boolean isQueue() {
        if (destinationType == JMSConstants.GENERIC) {
            return null;
        } else {
            return destinationType == JMSConstants.QUEUE;
        }
    }

    private void logError(String msg, Exception e) {
        log.error(msg, e);
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new JMSInputAdaptorRuntimeException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new JMSInputAdaptorRuntimeException(msg);
    }

    // -------------- getters and setters ------------------
    public String getEventAdapterName() {
        return eventAdapterName;
    }

    public void setEventAdapterName(String eventAdapterName) {
        this.eventAdapterName = eventAdapterName;
    }

    public String getConnFactoryJNDIName() {
        return connFactoryJNDIName;
    }

    public void setConnFactoryJNDIName(String connFactoryJNDIName) {
        this.connFactoryJNDIName = connFactoryJNDIName;
    }

    public String getDestinationJNDIName() {
        return destinationJNDIName;
    }

    public void setDestinationJNDIName(String destinationJNDIName) {
        this.destinationJNDIName = destinationJNDIName;
    }

    public int getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(int destinationType) {
        this.destinationType = destinationType;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public int getTransactionality() {
        return transactionality;
    }

    public void setTransactionality(int transactionality) {
        this.transactionality = transactionality;
        sessionTransacted = (transactionality == BaseConstants.TRANSACTION_LOCAL);
    }

    public boolean isSessionTransacted() {
        return sessionTransacted;
    }

    public void setSessionTransacted(Boolean sessionTransacted) {
        if (sessionTransacted != null) {
            this.sessionTransacted = sessionTransacted;
            // sessionTransacted means local transactions are used, however !sessionTransacted does
            // not mean that JTA is used.
            // do not change the transactionality based on this value, since some of the transaction 
            // providers require JTA to be set and also sessionTransacted = true.
            // if (sessionTransacted) {
            //     transactionality = BaseConstants.TRANSACTION_LOCAL;
            // }
        }
    }

    public int getSessionAckMode() {
        return sessionAckMode;
    }

    public void setSessionAckMode(int sessionAckMode) {
        this.sessionAckMode = sessionAckMode;
    }

    public boolean isSubscriptionDurable() {
        return subscriptionDurable;
    }

    public void setSubscriptionDurable(Boolean subscriptionDurable) {
        if (subscriptionDurable != null) {
            this.subscriptionDurable = subscriptionDurable;
        }
    }

    public String getDurableSubscriberName() {
        return durableSubscriberName;
    }

    public void setDurableSubscriberName(String durableSubscriberName) {
        this.durableSubscriberName = durableSubscriberName;
    }

    public String getDurableSubscriberClientId() {
        return durableSubscriberClientId;
    }

    public boolean isPubSubNoLocal() {
        return pubSubNoLocal;
    }

    public void setPubSubNoLocal(Boolean pubSubNoLocal) {
        if (pubSubNoLocal != null) {
            this.pubSubNoLocal = pubSubNoLocal;
        }
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public int getMaxConcurrentConsumers() {
        return maxConcurrentConsumers;
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    public int getIdleTaskExecutionLimit() {
        return idleTaskExecutionLimit;
    }

    public void setIdleTaskExecutionLimit(int idleTaskExecutionLimit) {
        this.idleTaskExecutionLimit = idleTaskExecutionLimit;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public int getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(int cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public int getInitialReconnectDuration() {
        return initialReconnectDuration;
    }

    public void setInitialReconnectDuration(int initialReconnectDuration) {
        this.initialReconnectDuration = initialReconnectDuration;
    }

    public double getReconnectionProgressionFactor() {
        return reconnectionProgressionFactor;
    }

    public void setReconnectionProgressionFactor(double reconnectionProgressionFactor) {
        this.reconnectionProgressionFactor = reconnectionProgressionFactor;
    }

    public long getMaxReconnectDuration() {
        return maxReconnectDuration;
    }

    public void setMaxReconnectDuration(long maxReconnectDuration) {
        this.maxReconnectDuration = maxReconnectDuration;
    }

    public int getMaxMessagesPerTask() {
        return maxMessagesPerTask;
    }

    public void setMaxMessagesPerTask(int maxMessagesPerTask) {
        this.maxMessagesPerTask = maxMessagesPerTask;
    }

    public String getUserTransactionJNDIName() {
        return userTransactionJNDIName;
    }

    public void setUserTransactionJNDIName(String userTransactionJNDIName) {
        if (userTransactionJNDIName != null) {
            this.userTransactionJNDIName = userTransactionJNDIName;
        }
    }

    public boolean isCacheUserTransaction() {
        return cacheUserTransaction;
    }

    public void setCacheUserTransaction(Boolean cacheUserTransaction) {
        if (cacheUserTransaction != null) {
            this.cacheUserTransaction = cacheUserTransaction;
        }
    }

    public boolean isJmsSpec11() {
        return jmsSpec11;
    }

    public void setJmsSpec11(boolean jmsSpec11) {
        this.jmsSpec11 = jmsSpec11;
    }

    public Hashtable<String, String> getJmsProperties() {
        return jmsProperties;
    }

    public void addJmsProperties(Map<String, String> jmsProperties) {
        this.jmsProperties.putAll(jmsProperties);
    }

    public void removeJmsProperties(String key) {
        this.jmsProperties.remove(key);
    }

    public Context getContext() {
        return context;
    }

    public ConnectionFactory getConnectionFactory() {
        return conFactory;
    }

    public List<MessageListenerTask> getPollingTasks() {
        return pollingTasks;
    }

    public void setJmsMessageListener(JMSMessageListener jmsMessageListener) {
        this.jmsMessageListener = jmsMessageListener;
    }

    public void setWorkerPool(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    public int getActiveTaskCount() {
        return activeTaskCount;
    }

    /**
     * Get the number of existing JMS message consumers.
     *
     * @return the number of consumers
     */
    public int getConsumerCount() {
        return consumerCount.get();
    }

    public void setJMSTaskManagerState(int JMSTaskManagerState) {
        this.JMSTaskManagerState = JMSTaskManagerState;
    }
}
