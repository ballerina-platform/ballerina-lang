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

import ballerina/jballerina.java;

# Holds event data.
#
public type Event record {
    string name;
    int timestampMicros;
    map<string> tags;
};

# Holds span data.
#
public type Span record {
  string operationName;
  string traceId;
  string spanId;
  string parentId;
  map<string> tags;
  Event[] events;
};

# Get all the finished spans.
#
# + serviceName - The name of the service of which the finished spans should be fetched
# + return - The finished spans
public isolated function getFinishedSpans(string serviceName) returns Span[] {
    handle serviceNameHandle = java:fromString(serviceName);
    json spansJson = externGetFinishedSpans(serviceNameHandle);
    Span[] | error spans = spansJson.cloneWithType();
    if (spans is error) {
        panic error("cannot convert to Span record array; json string: " + spansJson.toJsonString(), spans);
    } else {
        return spans;
    }
}

# Get all the finished spans.
#
# + serviceName - The name of the service of which the finished spans should be fetched
# + return - The finished spans as a json
isolated function externGetFinishedSpans(handle serviceName) returns json = @java:Method {
    name: "getFinishedSpans",
    'class: "org.ballerinalang.observe.mockextension.MockTracerUtils"
} external;
