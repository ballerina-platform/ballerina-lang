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

import org.ballerinalang.broker.BallerinaBroker;
import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    //TODO: check if this could be removed
    private ProgramFile hubProgramFile;

    // TODO: 9/23/18 make CopyOnWriteArrayList?
    private List<String> topics = new ArrayList<>();
    private List<HubSubscriber> subscribers = new ArrayList<>();

    private static final String BASE_PATH = "/websub";
    private static final String HUB_PATH = "/hub";

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

    public void registerTopic(String topic, boolean loadingOnStartUp) throws BallerinaWebSubException {
        if (isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic registration not allowed at the Hub: topic already exists");
        } else if (topic == null || topic.isEmpty()) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for registration at Hub");
        } else {
            topics.add(topic);
            if (hubPersistenceEnabled && !loadingOnStartUp) {
                Object[] args = {"register", topic};
                //TODO need API to call floating ballerina methods
//                FunctionInfo functionInfo = hubProgramFile.getPackageInfo(WEBSUB_PACKAGE)
//                        .getFunctionInfo("persistTopicRegistrationChange");
//                BVMExecutor.executeFunction(hubProgramFile, functionInfo, args);
            }
        }
    }

    public void unregisterTopic(String topic) throws BallerinaWebSubException {
        if (topic == null || !isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for unregistration at Hub");
        } else {
            topics.remove(topic);
            if (hubPersistenceEnabled) {
                Object[] args = {"unregister", topic};
                //TODO need API to call floating ballerina methods
//                FunctionInfo functionInfo = hubProgramFile.getPackageInfo(WEBSUB_PACKAGE)
//                        .getFunctionInfo("persistTopicRegistrationChange");
//                BVMExecutor.executeFunction(hubProgramFile, functionInfo, args);
            }
        }
    }

    public boolean isTopicRegistered(String topic) {
        return topics.contains(topic);
    }

    /**
     * Method to add a subscription to the topic on MB.
     *
     * @param topic     the topic to which the subscription should be added
     * @param callback  the callback registered for the particular subscription
     * @param subscriptionDetails the subscription details
     */
    public void registerSubscription(String topic, String callback, MapValue<String, Object> subscriptionDetails) {
        if (!started) {
            //TODO: Revisit to check if this needs to be returned as an error, currently not required since this check
            // is performed at Ballerina level
            logger.error("Hub Service not started: subscription failed");
        } else if (!topics.contains(topic) && hubTopicRegistrationRequired) {
            logger.warn("Subscription request ignored for unregistered topic[" + topic + "]");
        } else {
            if (getSubscribers().contains(new HubSubscriber("", topic, callback, null))) {
                unregisterSubscription(topic, callback);
            }
            String queue = UUID.randomUUID().toString();

            HubSubscriber subscriberToAdd = new HubSubscriber(queue, topic, callback, subscriptionDetails);
            brokerInstance.addSubscription(topic, subscriberToAdd);
            getSubscribers().add(subscriberToAdd);
        }
    }

    /**
     * Method to remove a subscription to the topic on MB.
     *
     * @param topic     the topic to which the subscription should was added
     * @param callback  the callback registered for the particular subscription
     */
    public void unregisterSubscription(String topic, String callback) {
        if (!started) {
            logger.error("Hub Service not started: unsubscription failed.");
            return;
        }
        HubSubscriber subscriberToUnregister = new HubSubscriber("", topic, callback, null);
        if (!getSubscribers().contains(subscriberToUnregister)) {
            if (callback.endsWith("/")) {
                unregisterSubscription(topic, callback.substring(0, callback.length() - 1));
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
     * @param topicRegistrationRequired whether a topic needs to be registered at the hub prior to
     *                                  publishing/subscribing to the topic
     * @param publicUrl                 the URL for the hub to be included in content delivery requests
     * @param hubListener               the http:Listener to which the hub service is attached
     */
    @SuppressWarnings("unchecked")
    public void startUpHubService(boolean topicRegistrationRequired, String publicUrl, ObjectValue hubListener) {
        synchronized (this) {
            if (!isStarted()) {
                try {
                    brokerInstance = BallerinaBroker.getBrokerInstance();
                } catch (Exception e) {
                    throw new BallerinaException("Error starting up internal broker for WebSub Hub");
                }
                //TODO need API to call floating ballerina methods
//                ProgramFile hubProgramFile = context.getProgramFile();
//                PackageInfo hubPackageInfo = hubProgramFile.getPackageInfo(WEBSUB_PACKAGE);
//
//                if (hubPackageInfo != null) {
//                    hubTopicRegistrationRequired = topicRegistrationRequired.booleanValue();
//                    String hubUrl = populateHubUrl(publicUrl, hubListener);
//                    Object[] args = new Object[0];
//                    //TODO: change once made public and available as a param
//                    hubPersistenceEnabled = Boolean.parseBoolean((BVMExecutor.executeFunction(hubProgramFile,
//                        hubPackageInfo.getFunctionInfo("isHubPersistenceEnabled"), args)[0]).stringValue());
//
//                    PrintStream console = System.err;
//                    console.println("[ballerina/websub] Default Ballerina WebSub Hub started up at " + hubUrl);
//                    setHubProgramFile(hubProgramFile);
//                    started = true;
//                    BVMExecutor.executeFunction(hubProgramFile, hubPackageInfo
//                            .getFunctionInfo("setupOnStartup"), args);
//                    setHubUrl(hubUrl);
//                    setHubObject(BLangConnectorSPIUtil.createObject(context, WEBSUB_PACKAGE,
//                                                     STRUCT_WEBSUB_BALLERINA_HUB, new BString(hubUrl), hubListener));
//                }
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
                setHubProgramFile(null);
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

    /**
     * Method to retrieve the program file for the WebSub Hub service.
     *
     * @return the program file returned when compiling the package at Hub start up
     */
    ProgramFile getHubProgramFile() {
        return hubProgramFile;
    }

    /**
     * Method to set the program file for the WebSub Hub service.
     *
     * @param programFile the program file result to set
     */
    private void setHubProgramFile(ProgramFile programFile) {
        hubProgramFile = programFile;
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
