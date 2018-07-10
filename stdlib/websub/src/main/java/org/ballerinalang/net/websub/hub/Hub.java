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

import org.ballerinalang.bre.Context;
import org.ballerinalang.broker.BallerinaBroker;
import org.ballerinalang.broker.BallerinaBrokerByteBuf;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_BALLERINA_HUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_SECRET;
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
    private BMap<String, BValue> hubObject = null;
    private BValue hubEndpoint = null;
    private String hubUrl;
    private boolean hubTopicRegistrationRequired;
    private boolean hubPersistenceEnabled;
    private volatile boolean started = false;

    //TODO: check if this could be removed
    private ProgramFile hubProgramFile;

    private Map<String, String> topics = new HashMap<>();
    private List<HubSubscriber> subscribers = new ArrayList<>();

    public static Hub getInstance() {
        return instance;
    }

    private Hub() {
    }

    private void setHubEndpoint(BValue hubEndpoint) {
        this.hubEndpoint = hubEndpoint;
    }

    public BValue getHubEndpoint() {
        return hubEndpoint;
    }

    private void setHubUrl(String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    private void setHubObject(BMap<String, BValue> hubObject) {
        this.hubObject = hubObject;
    }

    public BMap<String, BValue> getHubObject() {
        return hubObject;
    }

    public void registerTopic(String topic, String secret, boolean loadingOnStartUp) throws BallerinaWebSubException {
        if (!hubTopicRegistrationRequired) {
            throw new BallerinaWebSubException("Remote topic registration not allowed/not required at the Hub");
        }
        if (isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic registration not allowed at the Hub: topic already exists");
        } else if (topic == null || topic.isEmpty()) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for registration at Hub");
        } else {
            topics.put(topic, secret);
            if (hubPersistenceEnabled && !loadingOnStartUp) {
                BValue[] args = { new BString("register"), new BString(topic), new BString(secret) };
                BLangFunctions.invokeCallable(hubProgramFile.getPackageInfo(WEBSUB_PACKAGE)
                                              .getFunctionInfo("changeTopicRegistrationInDatabase"), args);
            }
        }
    }

    public void unregisterTopic(String topic, String secret) throws BallerinaWebSubException {
        if (!hubTopicRegistrationRequired) {
            throw new BallerinaWebSubException("Remote topic unregistration not allowed/not required at the Hub");
        }
        if (topic == null || !isTopicRegistered(topic)) {
            throw new BallerinaWebSubException("Topic unavailable/invalid for unregistration at Hub");
        } else if (!topics.get(topic).equals(secret)) {
            throw new BallerinaWebSubException("Topic unregistration denied at Hub for incorrect secret");
        } else {
            topics.remove(topic);
            if (hubPersistenceEnabled) {
                BValue[] args = { new BString("unregister"), new BString(topic), new BString(secret) };
                BLangFunctions.invokeCallable(hubProgramFile.getPackageInfo(WEBSUB_PACKAGE)
                                              .getFunctionInfo("changeTopicRegistrationInDatabase"), args);
            }
        }
    }

    public String retrievePublisherSecret(String topic) {
        //check for existence skipped since existence is checked via websub:isTopicRegistered(topic) initially
        return topics.get(topic);
    }

    public boolean isTopicRegistered(String topic) {
        return topics.containsKey(topic);
    }

    /**
     * Method to add a subscription to the topic on MB.
     *
     * @param topic     the topic to which the subscription should be added
     * @param callback  the callback registered for the particular subscription
     */
    public void registerSubscription(String topic, String callback, BMap<String, BValue> subscriptionDetails) {
        if (!started) {
            //TODO: Revisit to check if this needs to be returned as an error, currently not required since this check
            // is performed at Ballerina level
            logger.error("Hub Service not started: subscription failed");
        } else if (!topics.containsKey(topic) && hubTopicRegistrationRequired) {
            logger.warn("Subscription request ignored for unregistered topic[" + topic + "]");
        } else {
            if (subscribers.contains(new HubSubscriber("", topic, callback, null))) {
                unregisterSubscription(topic, callback);
            }
            String queue = UUID.randomUUID().toString();

            //Temporary workaround - expected secret to be "" if not specified but got null
            if (BLangConnectorSPIUtil.toStruct(subscriptionDetails).getStringField("secret") == null) {
                subscriptionDetails.put(SUBSCRIPTION_DETAILS_SECRET, new BString(""));
            }
            HubSubscriber subscriberToAdd = new HubSubscriber(queue, topic, callback, subscriptionDetails);
            brokerInstance.addSubscription(topic, subscriberToAdd);
            subscribers.add(subscriberToAdd);
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
        if (!subscribers.contains(subscriberToUnregister)) {
            if (callback.endsWith("/")) {
                unregisterSubscription(topic, callback.substring(0, callback.length() - 1));
            }
            return;
        } else {
            for (HubSubscriber subscriber:subscribers) {
                if (subscriber.equals(subscriberToUnregister)) {
                    subscriberToUnregister = subscriber;
                    break;
                }
            }
        }
        brokerInstance.removeSubscription(subscriberToUnregister);
        subscribers.remove(subscriberToUnregister);
    }

    /**
     * Method to publish to a topic on MB, a request to send to subscribers.
     *
     * @param topic             the topic to which the update should happen
     * @param content           the content to send, with payload and content type set
     * @throws BallerinaWebSubException if the hub service is not started or topic registration is required, but the
     *                                  topic is not registered
     */
    public void publish(String topic, BMap<String, BValue> content) throws BallerinaWebSubException {
        if (!started) {
            throw new BallerinaWebSubException("Hub Service not started: publish failed");
        } else if (!topics.containsKey(topic) && hubTopicRegistrationRequired) {
            throw new BallerinaWebSubException("Publish call ignored for unregistered topic[" + topic + "]");
        } else {
            brokerInstance.publish(topic, new BallerinaBrokerByteBuf(content));
        }
    }

    /**
     * Method to publish to a topic on MB.
     *
     * @param topic             the topic to which the update should happen
     * @param stringPayload     the update payload as a string
     * @throws BallerinaWebSubException if the hub service is not started or topic registration is required, but the
     *                                  topic is not registered
     */
    public void publish(String topic, String stringPayload) throws BallerinaWebSubException {
        if (!started) {
            throw new BallerinaWebSubException("Hub Service not started: publish failed");
        } else if (!topics.containsKey(topic) && hubTopicRegistrationRequired) {
            throw new BallerinaWebSubException("Publish call ignored for unregistered topic[" + topic + "]");
        } else {
            byte[] payload = stringPayload.getBytes(StandardCharsets.UTF_8);
            brokerInstance.publish(topic, payload);
        }
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Method to start up the default Ballerina WebSub Hub.
     */
    public void startUpHubService(Context context, BBoolean topicRegistrationRequired, BString publicUrl) {
        synchronized (this) {
            if (!isStarted()) {
                try {
                    brokerInstance = BallerinaBroker.getBrokerInstance();
                } catch (Exception e) {
                    throw new BallerinaException("Error starting up internal broker for WebSub Hub");
                }
                ProgramFile hubProgramFile = context.getProgramFile();
                PackageInfo hubPackageInfo = hubProgramFile.getPackageInfo(WEBSUB_PACKAGE);
                if (hubPackageInfo != null) {
                    BValue[] returns = BLangFunctions.invokeCallable(hubPackageInfo.getFunctionInfo("startHubService"),
                                                                     new BValue[]{});
                    hubTopicRegistrationRequired = topicRegistrationRequired.booleanValue();

                    String hubUrl = publicUrl.stringValue();
                    BValue[] args = new BValue[0];
                    //TODO: change once made public and available as a param
                    hubPersistenceEnabled = Boolean.parseBoolean((BLangFunctions.invokeCallable(
                        hubPackageInfo.getFunctionInfo("isHubPersistenceEnabled"), args)[0]).stringValue());

                    PrintStream console = System.err;
                    console.println("ballerina: Default Ballerina WebSub Hub started up at " + hubUrl);
                    setHubProgramFile(hubProgramFile);
                    started = true;
                    BLangFunctions.invokeCallable(hubPackageInfo.getFunctionInfo("setupOnStartup"), args);
                    setHubEndpoint(returns[0]);
                    setHubUrl(hubUrl);
                    setHubObject(BLangConnectorSPIUtil.createObject(context, WEBSUB_PACKAGE,
                                                     STRUCT_WEBSUB_BALLERINA_HUB, new BString(hubUrl), returns[0]));
                }
            } else {
                throw new BallerinaWebSubException("Hub Service already started up");
            }
        }
    }

    /**
     * Method to clean up after stopping the default Ballerina WebSub Hub.
     */
    public void stopHubService(Context context) {
        synchronized (this) {
            if (isStarted()) {
                started = false;
                setHubObject(null);
                setHubUrl(null);
                setHubProgramFile(null);
                hubTopicRegistrationRequired = false;
                if (hubPersistenceEnabled) {
                    BLangFunctions.invokeCallable(context.getProgramFile().getPackageInfo(WEBSUB_PACKAGE)
                                                    .getFunctionInfo("clearSubscriptionDataInDb"),
                                                  new BValue[]{});
                }
                hubPersistenceEnabled = false;
                topics = new HashMap<>();
                for (HubSubscriber subscriber : subscribers) {
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
     * Method to set the program file for the WebSub Hub service.
     *
     * @param programFile the program file result to set
     */
    private void setHubProgramFile(ProgramFile programFile) {
        hubProgramFile = programFile;
    }

    /**
     * Method to retrieve the program file for the WebSub Hub service.
     *
     * @return the program file returned when compiling the package at Hub start up
     */
    public ProgramFile getHubProgramFile() {
        return hubProgramFile;
    }

}
