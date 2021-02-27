// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

// Configurations
final configurable boolean enabled = false;
final configurable string provider = "";
final configurable boolean metricsEnabled = false;
final configurable string metricsReporter = "prometheus";
final configurable boolean tracingEnabled = false;
final configurable string tracingProvider = "jaeger";

function init() {
    externInitializeModule();
}

public isolated function isObservabilityEnabled() returns boolean = @java:Method {
    name: "isObservabilityEnabled",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

public isolated function isMetricsEnabled() returns boolean = @java:Method {
    name: "isMetricsEnabled",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

public isolated function getMetricsProvider() returns string = @java:Method {
    name: "getMetricsProvider",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

public isolated function getMetricsReporter() returns string = @java:Method {
    name: "getMetricsReporter",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

public isolated function isTracingEnabled() returns boolean = @java:Method {
    name: "isTracingEnabled",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

public isolated function getTracingProvider() returns string = @java:Method {
    name: "getTracingProvider",
    'class: "io.ballerina.runtime.observability.ObserveUtils"
} external;

function externInitializeModule() = @java:Method {
    'class: "org.ballerinalang.observe.nativeimpl.Utils",
    name: "initializeModule"
} external;
