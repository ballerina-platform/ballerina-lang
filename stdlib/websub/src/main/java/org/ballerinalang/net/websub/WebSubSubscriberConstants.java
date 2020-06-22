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

package org.ballerinalang.net.websub;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;
import static org.ballerinalang.jvm.util.BLangConstants.VERSION_SEPARATOR;

/**
 * Constants for WebSubSubscriber Services.
 *
 * @since 0.965.0
 */
public class WebSubSubscriberConstants {

    public static final String GENERIC_SUBSCRIBER_SERVICE_TYPE = "Service";
    public static final String WEBSUB_SERVICE_REGISTRY = "WEBSUB_SERVICE_REGISTRY";

    public static final String SERVICE_ENDPOINT_CONFIG_NAME = "config";
    public static final String ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG = "SubscriberServiceConfig";
    public static final String ANN_NAME_WEBSUB_SPECIFIC_SUBSCRIBER = "SpecificSubscriber";
    public static final String BALLERINA = "ballerina";
    public static final String WEBSUB = "websub";
    public static final String MODULE_VERSION = "1.0.0";
    public static final String GENERATED_PACKAGE_VERSION = MODULE_VERSION.replace(".", "_");
    public static final String WEBSUB_PACKAGE = BALLERINA + ORG_NAME_SEPARATOR + WEBSUB;
    public static final String WEBSUB_PACKAGE_FULL_QUALIFIED_NAME = WEBSUB_PACKAGE + VERSION_SEPARATOR + MODULE_VERSION;

    public static final BPackage WEBSUB_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, WEBSUB, MODULE_VERSION);
    public static final String WEBSUB_SERVICE_LISTENER = "Listener";
    public static final String WEBSUB_SERVICE_CALLER = "Caller";
    public static final String WEBSUB_HTTP_ENDPOINT = "serviceEndpoint";
    public static final BString WEBSUB_SERVICE_NAME = StringUtils.fromString("webSubServiceName");

    public static final String ENDPOINT_CONFIG_HOST = "host";
    public static final String ENDPOINT_CONFIG_PORT = "port";
    public static final BString ENDPOINT_CONFIG_SECURE_SOCKET_CONFIG = StringUtils.fromString("secureSocket");

    public static final BString ANN_WEBSUB_ATTR_SUBSCRIBE_ON_STARTUP = StringUtils.fromString("subscribeOnStartUp");
    public static final BString ANN_WEBSUB_ATTR_TARGET = StringUtils.fromString("target");
    public static final BString ANN_WEBSUB_ATTR_LEASE_SECONDS = StringUtils.fromString("leaseSeconds");
    public static final BString ANN_WEBSUB_ATTR_SECRET = StringUtils.fromString("secret");
    public static final BString ANN_WEBSUB_ATTR_CALLBACK = StringUtils.fromString("callback");
    public static final String ANN_WEBSUB_ATTR_EXPECT_INTENT_VERIFICATION = "expectIntentVerification";
    public static final BString ANN_WEBSUB_ATTR_SUBSCRIPTION_PUBLISHER_CLIENT_CONFIG = StringUtils.fromString(
            "publisherClientConfig");
    public static final BString ANN_WEBSUB_ATTR_SUBSCRIPTION_HUB_CLIENT_CONFIG = StringUtils.fromString(
            "hubClientConfig");

    public static final String TOPIC_ID_HEADER = "TOPIC_ID_HEADER";
    public static final String TOPIC_ID_PAYLOAD_KEY = "TOPIC_ID_PAYLOAD_KEY";
    public static final String TOPIC_ID_HEADER_AND_PAYLOAD = "TOPIC_ID_HEADER_AND_PAYLOAD";

    public static final String STRUCT_WEBSUB_BALLERINA_HUB = "Hub";
    public static final String STRUCT_WEBSUB_BALLERINA_HUB_STARTED_UP_ERROR = "HubStartedUpError";

    static final BString PARAM_HUB_MODE = StringUtils.fromString("hub.mode");
    static final BString PARAM_HUB_TOPIC = StringUtils.fromString("hub.topic");
    static final BString PARAM_HUB_CHALLENGE = StringUtils.fromString("hub.challenge");
    static final BString PARAM_HUB_LEASE_SECONDS = StringUtils.fromString("hub.lease_seconds");

    static final BString SUBSCRIBE = StringUtils.fromString("subscribe");
    static final BString UNSUBSCRIBE = StringUtils.fromString("unsubscribe");

    static final String ANNOTATED_TOPIC = "annotatedTopic";
    static final String DEFERRED_FOR_PAYLOAD_BASED_DISPATCHING = "deferredForPayloadBasedDispatching";
    static final String ENTITY_ACCESSED_REQUEST = "entityAccessedRequest";

    public static final String WEBSUB_INTENT_VERIFICATION_REQUEST = "IntentVerificationRequest";
    public static final String WEBSUB_NOTIFICATION_REQUEST = "Notification";

    public static final String RESOURCE_NAME_ON_INTENT_VERIFICATION = "onIntentVerification";
    public static final String RESOURCE_NAME_ON_NOTIFICATION = "onNotification";

    static final BString PATH_FIELD = StringUtils.fromString("path");

    // SubscriptionDetails struct field names
    public static final String SUBSCRIPTION_DETAILS_TOPIC = "topic";
    public static final String SUBSCRIPTION_DETAILS_CALLBACK = "callback";
    public static final String SUBSCRIPTION_DETAILS_SECRET = "secret";
    public static final BString SUBSCRIPTION_DETAILS_LEASE_SECONDS = StringUtils.fromString("leaseSeconds");
    public static final BString SUBSCRIPTION_DETAILS_CREATED_AT = StringUtils.fromString("createdAt");
    public static final String SUBSCRIPTION_DETAILS = "SubscriberDetails";

    // IntentVerificationRequest
    public static final String VERIFICATION_REQUEST_MODE = "mode";
    public static final String VERIFICATION_REQUEST_TOPIC = "topic";
    public static final String VERIFICATION_REQUEST_CHALLENGE = "challenge";
    public static final String VERIFICATION_REQUEST_LEASE_SECONDS = "leaseSeconds";
    public static final String REQUEST = "request";

    // websub Listener struct
    public static final String LISTENER_SERVICE_ENDPOINT_CONFIG = "config";
    public static final String LISTENER_SERVICE_ENDPOINT = "serviceEndpoint";

    // SubscriberServiceEndpointConfiguration struct
    public static final String SERVICE_CONFIG_HOST = "host";
    public static final String SERVICE_CONFIG_PORT = "port";
    public static final String SERVICE_CONFIG_SECURE_SOCKET = "httpServiceSecureSocket";
    public static final String SERVICE_CONFIG_EXTENSION_CONFIG = "extensionConfig";
    public static final String EXTENSION_CONFIG_TOPIC_IDENTIFIER = "topicIdentifier";
    public static final BString EXTENSION_CONFIG_HEADER_RESOURCE_MAP = StringUtils.fromString("headerResourceMap");
    public static final BString EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP = StringUtils.fromString(
            "payloadKeyResourceMap");
    public static final BString EXTENSION_CONFIG_HEADER_AND_PAYLOAD_KEY_RESOURCE_MAP = StringUtils.fromString(
            "headerAndPayloadKeyResourceMap");

    public static final String EXTENSION_CONFIG_TOPIC_HEADER = "topicHeader";

    public static final String SERVICE_CONFIG_TOPIC_PAYLOAD_KEYS = "topicPayloadKeys";
    public static final String SERVICE_CONFIG_TOPIC_RESOURCE_MAP = "topicResourceMap";

    // WebSub error types related constants
    public static final String WEBSUB_LISTENER_STARTUP_FAILURE = "ListenerStartupError";
}
