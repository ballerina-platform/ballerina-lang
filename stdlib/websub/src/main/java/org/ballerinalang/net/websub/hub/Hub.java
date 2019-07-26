/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.websub.hub;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.broker.BallerinaBroker;
import org.ballerinalang.net.websub.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.ballerinalang.jvm.values.connector.Executor.executeFunction;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.BALLERINA;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_BALLERINA_HUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * The Ballerina WebSub Hub.
 *
 * @since 0.965.0
 */
public class Hub {
    private static final Logger logger = LoggerFactory.getLogger(Hub.class);

    private static Hub instance = new Hub();
    private BallerinaBroker brokerInstance = null;
    private ObjectValue hubObject = null;
    private String hubUrl;
    private boolean hubTopicRegistrationRequired;
    private boolean hubPersistenceEnabled;
    private volatile boolean started = false;

    // TODO: 9/23/18 make CopyOnWriteArrayList?
    private List<String> topics = new ArrayList<>();
    private List<HubSubscriber> subscribers = new ArrayList<>();
    private ClassLoader classLoader = this.getClass().getClassLoader();

    private static final String BASE_PATH = "/websub";
    private static final String HUB_PATH = "/hub";
    private static final String HUB_SERVICE = "hub_service";

    public static Hub getInstance() {
        return instance;
    }

    private Hub() {
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public ObjectValue getHubObject() {
        return hubObject;
    }

    public void registerTopic(Strand strand, String topic, boolean loadingOnStartUp) throws BallerinaWebSubException {
        if (isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic registration not allowed at the Hub: topic already exists");
        } else if (topic == null || topic.isEmpty()) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for registration at Hub");
        } else {
            topics.add(topic);
            if (hubPersistenceEnabled && !loadingOnStartUp) {
                Object[] args = {"register", topic};
                executeFunction(strand.scheduler, classLoader, BALLERINA, WEBSUB, HUB_SERVICE,
                                "persistTopicRegistrationChange", args);
            }
        }
    }

    public void unregisterTopic(Strand strand, String topic) throws BallerinaWebSubException {
        if (topic == null || !isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for unregistration at Hub");
        } else {
            topics.remove(topic);
            if (hubPersistenceEnabled) {
                Object[] args = {"unregister", topic};
                executeFunction(strand.scheduler, classLoader, BALLERINA, WEBSUB, HUB_SERVICE,
                                "persistTopicRegistrationChange", args);
            }
        }
    }

    public boolean isTopicRegistered(String topic) {
        return topics.contains(topic);
    }

    /**
     * Method to add a subscription to the topic on MB.
     * @param strand    the current strand
     * @param topic     the topic to which the subscription should be added
     * @param callback  the callback registered for the particular subscription
     * @param subscriptionDetails the subscription details
     */
    public void registerSubscription(Strand strand, String topic, String callback,
                                     MapValue<String, Object> subscriptionDetails) {
        if (!started) {
            //TODO: Revisit to check if this needs to be returned as an error, currently not required since this check
            // is performed at Ballerina level
            logger.error("Hub Service not started: subscription failed");
        } else if (!topics.contains(topic) && hubTopicRegistrationRequired) {
            logger.warn("Subscription request ignored for unregistered topic[" + topic + "]");
        } else {
            if (getSubscribers().contains(new HubSubscriber(strand, "", topic, callback, null))) {
                unregisterSubscription(strand, topic, callback);
            }
            String queue = UUID.randomUUID().toString();

            HubSubscriber subscriberToAdd = new HubSubscriber(strand, queue, topic, callback, subscriptionDetails);
            brokerInstance.addSubscription(topic, subscriberToAdd);
            getSubscribers().add(subscriberToAdd);
        }
    }

    /**
     * Method to remove a subscription to the topic on MB.
     *
     * @param strand    the current strand
     * @param topic     the topic to which the subscription should was added
     * @param callback  the callback registered for the particular subscription
     */
    public void unregisterSubscription(Strand strand, String topic, String callback) {
        if (!started) {
            logger.error("Hub Service not started: unsubscription failed.");
            return;
        }
        HubSubscriber subscriberToUnregister = new HubSubscriber(strand, "", topic, callback, null);
        if (!getSubscribers().contains(subscriberToUnregister)) {
            if (callback.endsWith("/")) {
                unregisterSubscription(strand, topic, callback.substring(0, callback.length() - 1));
            }
            return;
        } else {
            for (HubSubscriber subscriber: getSubscribers()) {
                if (subscriber.equals(subscriberToUnregister)) {
                    subscriberToUnregister = subscriber;
                    break;
                }
            }
        }
        brokerInstance.removeSubscription(subscriberToUnregister);
        getSubscribers().remove(subscriberToUnregister);
    }

    /**
     * Method to publish to a topic on MB, a request to send to subscribers.
     *
     * @param topic             the topic to which the update should happen
     * @param content           the content to send, with payload and content type set
     * @throws BallerinaWebSubException if the hub service is not started or topic registration is required, but the
     *                                  topic is not registered
     */
    public void publish(String topic, MapValue<String, Object> content) throws BallerinaWebSubException {
        if (!started) {
            throw new BallerinaWebSubException("Hub Service not started: publish failed");
        } else if (!topics.contains(topic) && hubTopicRegistrationRequired) {
            throw new BallerinaWebSubException("Publish call ignored for unregistered topic[" + topic + "]");
        } else {
            brokerInstance.publish(topic, new BallerinaBrokerByteBuf(content));
        }
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Method to start up the default Ballerina WebSub Hub.
     *
     * @param strand                    current strand
     * @param topicRegistrationRequired whether a topic needs to be registered at the hub prior to
     *                                  publishing/subscribing to the topic
     * @param publicUrl                 the URL for the hub to be included in content delivery requests
     * @param hubListener               the http:Listener to which the hub service is attached
     */
    @SuppressWarnings("unchecked")
    public void startUpHubService(Strand strand, boolean topicRegistrationRequired, String publicUrl,
                                  ObjectValue hubListener) {
        synchronized (this) {
            if (!isStarted()) {
                try {
                    brokerInstance = BallerinaBroker.getBrokerInstance();
                } catch (Exception e) {
                    throw new BallerinaException("Error starting up internal broker for WebSub Hub");
                }
                hubTopicRegistrationRequired = topicRegistrationRequired;
                String hubUrl = populateHubUrl(publicUrl, hubListener);
                //TODO: change once made public and available as a param
                Object returnValue = executeFunction(strand.scheduler, classLoader, BALLERINA, WEBSUB,
                                                     "hub_configuration", "isHubPersistenceEnabled");
                hubPersistenceEnabled = Boolean.parseBoolean(returnValue.toString());

                PrintStream console = System.err;
                console.println("[ballerina/websub] Default Ballerina WebSub Hub started up at " + hubUrl);
                started = true;
                executeFunction(strand.scheduler, classLoader, BALLERINA, WEBSUB, HUB_SERVICE, "setupOnStartup");
                setHubUrl(hubUrl);
                setHubObject(BallerinaValues.createObjectValue(WEBSUB_PACKAGE, STRUCT_WEBSUB_BALLERINA_HUB, hubUrl,
                                                               hubListener));
            } else {
                throw new BallerinaWebSubException("Hub Service already started up");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String populateHubUrl(String hubUrl, ObjectValue hubListener) {
        if (hubUrl.isEmpty()) {
            String hubPort = String.valueOf(hubListener.get("port"));
            Object secureSocket = ((MapValue<String, Object>) hubListener.get("config")).get("secureSocket");
            hubUrl = secureSocket != null ? ("https://localhost:" + hubPort + BASE_PATH + HUB_PATH)
                    : ("http://localhost:" + hubPort + BASE_PATH + HUB_PATH);
        }
        return hubUrl;
    }

    /**
     * Method to clean up after stopping the default Ballerina WebSub Hub.
     */
    public void stopHubService() {
        synchronized (this) {
            if (isStarted()) {
                started = false;
                setHubObject(null);
                setHubUrl(null);
                hubTopicRegistrationRequired = false;
                hubPersistenceEnabled = false;
                topics = new ArrayList<>();
                for (HubSubscriber subscriber : getSubscribers()) {
                    brokerInstance.removeSubscription(subscriber);
                }
                subscribers = new ArrayList<>();
                brokerInstance = null;
            } else {
                throw new BallerinaWebSubException("Hub Service already stopped");
            }
        }
    }

    private void setHubUrl(String hubUrl) {
        this.hubUrl = hubUrl;
    }

    private void setHubObject(ObjectValue hubObject) {
        this.hubObject = hubObject;
    }

    /**
     * Retrieve available topics of the Hub.
     *
     * @return the array of topics
     */
    public String[] getTopics() {
        return topics.toArray(new String[0]);
    }

    /**
     * Retrieve subscribers list.
     *
     * @return the list of subscribers
     */
    public List<HubSubscriber> getSubscribers() {
        return subscribers;
    }
}
