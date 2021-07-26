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

public function main(int i, float f, string s, *Employee e, string... args) {
    string restArgs = "";
    foreach var str in args {
        restArgs += str + " ";
    }

    print("integer: " + i.toString() + ", float: " + f.toString() + ", string: " + s + ", Employee Name Field: " +
        e.name + ", string rest args: " + restArgs);
}

public type Employee record {
    string name = "";
};

function print(string str) {
    printVal(system_out(), java:fromString(str));
}

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function printVal(handle receiver, handle arg0) = @java:Method {
    name: "print",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

