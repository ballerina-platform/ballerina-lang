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
package ballerina.websub;

///////////////////////////
/// Service Annotations ///
///////////////////////////
@Description {value:"Configuration for a WebSubSubscriber service"}
@Field {value:"endpoints: Array of endpoints the service would be attached to"}
@Field {value:"basePath: Path of the WebSubSubscriber service"}
@Field {value:"subscribeOnStartUp: Whether a subscription request is expected to be sent on start up"}
@Field {value:"resourceUrl: The resource URL for which discovery will be initiated to identify hub and topic if not
specified."}
@Field {value:"hub: The hub at which the subscription should be registered."}
@Field {value:"topic: The topic to which this WebSub subscriber (callback) should be registered."}
@Field {value:"leaseSeconds: The period for which the subscription is expected to be active."}
@Field {value:"secret: The secret to be used for authenticated content distribution."}
@Field {value:"callback: The callback to use when registering, if unspecified host:port/path will be used."}
public type SubscriberServiceConfiguration {
    Listener[] endpoints,
    string basePath,
    boolean subscribeOnStartUp,
    string resourceUrl,
    string hub,
    string topic,
    int leaseSeconds,
    string secret,
    string callback,
};

@Description {value:"WebebSubSubscriber Configuration for service"}
public annotation <service> SubscriberServiceConfig SubscriberServiceConfiguration;
