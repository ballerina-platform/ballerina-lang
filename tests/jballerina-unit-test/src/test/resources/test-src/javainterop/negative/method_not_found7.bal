// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function getPrintableStackTrace(error s) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.values.BError"
} external;

function concat(string s1, string s2) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.values.BString"
} external;

function concatString(handle h, string s1, string s2) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.values.BString",
    name: "concat"
} external;

function indexOf() returns byte = @java:Method {
    'class: "java.lang.String"
} external;
