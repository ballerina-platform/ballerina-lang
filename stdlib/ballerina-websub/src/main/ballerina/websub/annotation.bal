// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

///////////////////////////
/// Service Annotations ///
///////////////////////////
documentation {
    Configuration for a WebSubSubscriber service.

    F{{endpoints}} Array of endpoints the service would be attached to
    F{{path}}  Path of the WebSubSubscriber service
    F{{subscribeOnStartUp}} Boolean indicating whether a subscription request is expected to be sent on start up
    F{{resourceUrl}}  The resource URL for which discovery will be initiated to identify hub and topic if not
                        specified
    F{{hub}} The hub at which the subscription should be registered
    F{{topic}} The topic for which this WebSub subscriber (callback) should be registered
    F{{leaseSeconds}} The period for which the subscription is expected to be active
    F{{secret}} The secret to be used for authenticated content distribution
    F{{callback}} The callback to use when registering, if unspecified host:port/path will be used
    F{{auth}} The auth configuration to use when subscribing at the hub
    F{{secureSocket}} The secure socket configuration to use when subscribing at the hub
    F{{followRedirects}} The HTTP redirect related configuration specifying if subscription requests should be sent
                            to redirected hubs/topics
}
public type SubscriberServiceConfiguration record {
    Listener[] endpoints,
    string path,
    boolean subscribeOnStartUp,
    string resourceUrl,
    string hub,
    string topic,
    int leaseSeconds,
    string secret,
    string callback,
    http:AuthConfig? auth,
    http:SecureSocket? secureSocket,
    http:FollowRedirects? followRedirects,
};

documentation {
    WebSub Subscriber Configuration for the service, indicating subscription related parameters.
}
public annotation<service> SubscriberServiceConfig SubscriberServiceConfiguration;
