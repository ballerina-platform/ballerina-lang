/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.WebSubUtils;
import org.ballerinalang.net.websub.hub.Hub;
import org.ballerinalang.net.websub.hub.HubSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_BALLERINA_HUB_STARTED_UP_ERROR;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_CALLBACK;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_CREATED_AT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIPTION_DETAILS_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_ID;

/**
 * This class contains interop external functions related to Hub.
 *
 * @since 1.1.0
 */
public class HubNativeOperationHandler {

    private static final Logger log = LoggerFactory.getLogger(HubNativeOperationHandler.class);

    /**
     * Retrieves topics currently recognized by the Hub.
     *
     * @param webSubHub respective hub instance
     * @return an array of available topics
     */
    public static ArrayValue getAvailableTopics(ObjectValue webSubHub) {
        String[] topics = Hub.getInstance().getTopics();
        return new ArrayValueImpl(StringUtils.fromStringArray(topics));
    }

    /**
     * Retrieves details of subscribers registered to receive updates for a particular topic.
     *
     * @param webSubHub respective hub instance
     * @param topic     the topic for which details need to be retrieved
     * @return an array of subscriber details
     */
    public static ArrayValue getSubscribers(ObjectValue webSubHub, BString topic) {
        ArrayValue subscriberDetailArray = null;
        try {
            List<HubSubscriber> subscribers = Hub.getInstance().getSubscribers();
            MapValue<BString, Object> subscriberDetailsRecordValue =
                    BallerinaValues.createRecordValue(WEBSUB_PACKAGE_ID, SUBSCRIPTION_DETAILS);
            subscriberDetailArray = new ArrayValueImpl(new BArrayType(subscriberDetailsRecordValue.getType()));
            for (HubSubscriber subscriber : subscribers) {
                if (topic.getValue().equals(subscriber.getTopic())) {
                    MapValue<BString, Object> subscriberDetail = BallerinaValues.createRecord(
                            subscriberDetailsRecordValue, subscriber.getCallback(),
                            subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_LEASE_SECONDS),
                            subscriber.getSubscriptionDetails().get(SUBSCRIPTION_DETAILS_CREATED_AT));
                    subscriberDetailArray.append(subscriberDetail);
                }
            }
        } catch (Exception ex) {
            log.error("Error occurred while getting available subscribers.", ex);
        }
        return subscriberDetailArray;
    }

    /**
     * Starts up the default Ballerina WebSub Hub.
     *
     * @param basePath                  the base path of the hub service
     * @param subscriptionResourcePath  the resource path for subscription
     * @param publishResourcePath       the resource path for publishing and topic registration
     * @param topicRegistrationRequired whether a topic needs to be registered at the hub prior to
     *                                  publishing/subscribing to the topic
     * @param publicUrl                 the URL for the hub to be included in content delivery requests, defaults to
     *                                  `http(s)://localhost:{port}/websub/hub` if unspecified
     * @param hubListener               the `http:Listener` to which the hub service is attached
     * @return `Hub` the WebSub Hub object representing the newly started up hub, or `HubStartedUpError` indicating that
     * the hub is already started, and including the WebSub Hub object representing the already started up hub
     */
    public static Object startUpHubService(BString basePath, BString subscriptionResourcePath,
                                           BString publishResourcePath, boolean topicRegistrationRequired,
                                           BString publicUrl, ObjectValue hubListener) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted()) {
            MapValue<BString, Object> hubStartedUpError =
                    BallerinaValues.createRecordValue(WEBSUB_PACKAGE_ID, STRUCT_WEBSUB_BALLERINA_HUB_STARTED_UP_ERROR);
            return BallerinaValues.createRecord(hubStartedUpError, "Ballerina Hub already started up", null,
                                                hubInstance.getHubObject());
        }
        return hubInstance.startUpHubService(Scheduler.getStrand(), basePath.getValue(),
                                             subscriptionResourcePath.getValue(), publishResourcePath.getValue(),
                                             topicRegistrationRequired, publicUrl.getValue(), hubListener);
    }

    /**
     * Stops the default Ballerina WebSub Hub, if started.
     *
     * @param hub the `websub:Hub` object returned when starting the hub
     * @return `()` if the Ballerina Hub had been started up and was stopped now, `error` if not
     */
    public static Object stopHubService(ObjectValue hub) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted()) {
            try {
                if (hubInstance.getHubObject() != hub) {
                    return WebSubUtils.createError("error stopping the hub service: hub object does not match the " +
                                                           "started hub");
                }
                hubInstance.stopHubService();
                return null;
            } catch (BallerinaWebSubException e) {
                return WebSubUtils.createError(e.getMessage());
            }
        }
        return WebSubUtils.createError("error stopping the hub service: hub service not started");
    }

    /**
     * Adds a new subscription for the specified topic in the Ballerina Hub.
     *
     * @param subscriptionDetails the details of the subscription including WebSub specifics
     */
    public static void addSubscription(MapValue<BString, Object> subscriptionDetails) {
        String topic = subscriptionDetails.getStringValue(
                StringUtils.fromString(SUBSCRIPTION_DETAILS_TOPIC)).getValue();
        String callback = subscriptionDetails.getStringValue(
                StringUtils.fromString(SUBSCRIPTION_DETAILS_CALLBACK)).getValue();
        Hub.getInstance().registerSubscription(Scheduler.getStrand(), topic, callback, subscriptionDetails);
    }

    /**
     * Publishes against a topic in the default Ballerina Hub's underlying broker.
     *
     * @param topic   the topic for which the update should happen
     * @param content the content to send to subscribers, with the payload and content-type specified
     * @return `error` if an error occurred during publishing
     */
    public static Object publishToInternalHub(BString topic, MapValue<BString, Object> content) {
        try {
            Hub.getInstance().publish(topic.getValue(), content);
        } catch (BallerinaWebSubException e) {
            return WebSubUtils.createError(e.getMessage());
        }
        return null;
    }

    /**
     * Removes a subscription from the default Ballerina Hub's underlying broker.
     *
     * @param topic    the topic for which the subscription was added
     * @param callback the callback registered for this subscription
     */
    public static void removeNativeSubscription(BString topic, BString callback) {
        Hub.getInstance().unregisterSubscription(Scheduler.getStrand(), topic.getValue(), callback.getValue());
    }

    /**
     * Registers a topic in the Ballerina Hub, to accept subscription requests against.
     *
     * @param topic the topic to register
     * @return `error` if an error occurred during the registration
     */
    public static Object registerTopicAtNativeHub(BString topic) {
        try {
            Hub.getInstance().registerTopic(topic.getValue());
        } catch (BallerinaWebSubException e) {
            return WebSubUtils.createError(e.getMessage());
        }
        return null;
    }

    /**
     * Unregisters a topic in the Ballerina Hub.
     *
     * @param topic the topic to unregister
     * @return `error` if an error occurred during the un-registration
     */
    public static Object unregisterTopicAtNativeHub(BString topic) {
        try {
            Hub.getInstance().unregisterTopic(topic.getValue());
        } catch (BallerinaWebSubException e) {
            return WebSubUtils.createError(e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a topic is registered in the Ballerina Hub.
     *
     * @param topic the topic to check
     * @return `boolean` True if the topic has been registered by a publisher, false if not
     */
    public static boolean isTopicRegistered(BString topic) {
        return Hub.getInstance().isTopicRegistered(topic.getValue());
    }
}
