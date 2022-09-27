// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.` `

import ballerina/jballerina.java;

handle outStreamObj = outStream();

isolated function print(handle printStream, any|error obj) = @java:Method {
    name: "print",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.Object"]
} external;

function println(any|error... objs) {
    foreach var obj in objs {
        print(outStreamObj, obj);
    }
    print(outStreamObj, "\n");
}

isolated function outStream() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

public isolated function split(string receiver, string delimiter) returns string[] {
    handle res = splitExternal(java:fromString(receiver), java:fromString(delimiter));
    return getBallerinaStringArray(res);
}

isolated function splitExternal(handle receiver, handle delimiter) returns handle = @java:Method {
    name: "split",
    'class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

isolated function getBallerinaStringArray(handle h) returns string[] = @java:Method {
    'class:"io.ballerina.runtime.api.utils.StringUtils",
    name:"fromStringArray",
    paramTypes:["[Ljava.lang.String;"]
} external;

isolated function writeContent(string filePath, string content) returns error? = @java:Method {
    'class:"org.ballerinalang.testerina.natives.io.FileUtils",
    name:"writeContent"
} external;

isolated function readContent(string filePath) returns string = @java:Method {
    'class:"org.ballerinalang.testerina.natives.io.FileUtils",
    name:"readContent"
} external;

isolated function fileExists(string filePath) returns boolean = @java:Method {
    'class:"org.ballerinalang.testerina.natives.io.FileUtils",
    name:"fileExists"
} external;

isolated function isSystemConsole() returns boolean = @java:Method {
    'class:"org.ballerinalang.testerina.natives.io.StringUtils",
    name:"isSystemConsole"
} external;

isolated function sprintf(string format, (any|error)... args) returns string = @java:Method {
    name : "sprintf",
    'class : "org.ballerinalang.testerina.natives.io.StringUtils"
} external;

isolated function matchWildcard(string functionName, string functionPattern) returns boolean|error = @java:Method {
    name : "matchWildcard",
    'class : "org.ballerinalang.testerina.natives.io.StringUtils"
} external;

isolated function decode(string str, string charset) returns string|error = @java:Method {
    name : "decode",
    'class : "org.ballerinalang.testerina.natives.io.StringUtils"
} external;

isolated function getBallerinaType((any|error) value) returns string = @java:Method {
    name : "getBallerinaType",
    'class : "org.ballerinalang.testerina.core.BallerinaTypeCheck"
} external;

isolated function getStringDiff(string actual, string expected) returns string = @java:Method {
     name : "getStringDiff",
     'class : "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
 } external;


isolated function getKeysDiff(string[] actualKeys, string[] expectedKeys) returns string = @java:Method {
    name: "getKeysDiff",
    'class: "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
} external;
