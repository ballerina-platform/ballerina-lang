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

isolated function init() {
    print("Initializing module 'current'");
    worker w1 {
        print("executing worker 'w1'");
        while true {
            
        }
    }
}

public isolated function main() {
    print("main function invoked for 'current' module");
}

isolated function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

isolated function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

isolated function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
