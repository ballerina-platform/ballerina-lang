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

import ballerina/java;
import ballerina/io;
import ballerina/runtime;

int totalNoOfStrandsForTest = 11;
int errorCount = 0;
int successCount = 0;
string[] errorMessages = [];

type Person object {
    public string name;
    function __init(string name) returns error? {
        _ = start assertStrandMetaDataResult("$anon/.:0.0.0.Person.__init-4");
        worker w2 {
            assertStrandMetaDataResult("$anon/.:0.0.0.Person.__init.w2-5");
        }
         _ =  @strand{name:"**my starnd inside worker**"}
                        start assertStrandMetaDataResult("$anon/.:0.0.0.Person.__init.**my starnd inside worker**-6");
        self.name = name;
    }
};


function testStrandMetaDataAsyncCalls() {
    Person|error p1 = new("Waruna");
    foo();
    assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.test-3");
    worker w1 {
        assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.w1-7");
    }
    future<()> f1 = start assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.f1-8");
    _ = start assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls-9");
    _ = start assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls-10");
    _ = @strand{name:"**my starnd**"}
            start assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.**my starnd**-11");

    function(string s) func = assertStrandMetaDataResult;
    future<()> x = start func("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.x-12");
    while (successCount < totalNoOfStrandsForTest && errorCount == 0) {
        runtime:sleep(1);
    }
    if (errorCount > 0) {
        errorMessages.forEach(function(string message) {
            io:println(message);
        });
        panic AssertionError(message = "Test failed due to errors.");
    }
}

function foo() {
    assertStrandMetaDataResult("$anon/.:0.0.0.testStrandMetaDataAsyncCalls.test-3");
}

function assertStrandMetaDataResult(string assertString) {
    string result = "";
    var strand = getStrand();
    var metadataOP = getMetaData(strand);
    boolean isMetadataAvailable = isPresent(metadataOP);
    if (isMetadataAvailable) {
        var metadata = get(metadataOP);
        int id = getId(strand);
        var strandName = getName(strand);
        var isStrandHasName = isPresent(strandName);
        string org = <string>java:toString(getModuleOrg(metadata));
        string modName = <string>java:toString(getModuleName(metadata));
        string modVersion = <string>java:toString(getModuleVersion(metadata));
        string parentFunc = <string>java:toString(getParentFunctionName(metadata));
        string typeName = "";
        var typeNameVal = java:toString(getTypeName(metadata));
        string name = "";
        if (isStrandHasName) {
            name = "."+ <string>java:toString(get(strandName));
        }
        if (typeNameVal is string) {
            typeName = "." + typeNameVal;
        }
        assertEquality(org +"/" + modName + ":" + modVersion + typeName + "." + parentFunc + name + "-" +
                        id.toString(), assertString);
    } else {
        errorMessages[errorCount] = "meta data cannot be found for evaluate assert string '" + assertString + "'";
        errorCount = errorCount + 1;
    }
}

function getName(handle starnd) returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function get(handle optional) returns handle = @java:Method {
    class: "java.util.Optional"
} external;

function isPresent(handle optional) returns boolean = @java:Method {
    class: "java.util.Optional"
} external;

function getId(handle starnd) returns int = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function getStrand() returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Scheduler"
} external;

function getMetaData(handle starnd) returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function getModuleOrg(handle starnd) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetaData",
    name : "moduleOrg"
} external;

function getModuleName(handle starnd) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetaData",
     name : "moduleName"
} external;

function getModuleVersion(handle starnd) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetaData",
    name : "moduleVersion"
} external;

function getParentFunctionName(handle starnd) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetaData",
    name : "parentFunctionName"
} external;

function getTypeName(handle starnd) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetaData",
    name : "typeName"

} external;

type AssertionError error<ASSERTION_ERROR_REASON>;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(string expected, string actual) {
    if expected == actual {
        successCount = successCount + 1;
        return;
    }
    errorMessages[errorCount] = "expected '" + expected + "', found '" + actual + "'";
    errorCount = errorCount + 1;
}
