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

int totalNoOfStrandsForTest = 14;
int errorCount = 0;
int successCount = 0;
string[] errorMessages = [];

class Person {
    public string name;
    function init(string name) {
        // async calls inside object
        _ = start assertStrandMetadataResult("$anon/.:0.anon");
        worker w2 {
            assertStrandMetadataResult("$anon/.:0.w2");
        }
         _ =  @strand{name:"**my strand inside object**"}
                        start assertStrandMetadataResult("$anon/.:0.**my strand inside object**");
        foo();
        self.name = name;
        // object function
        self.bar();
    }

    function bar() {
         assertStrandMetadataResult("$anon/.:0.test");
         // async call inside object function
          _ =  @strand{name:"**my strand inside object bar**"}
                start assertStrandMetadataResult("$anon/.:0.**my strand inside object bar**");
    }
}


function testStrandMetadataAsyncCalls() {
    Person p1 = new("Waruna");
    // inside same function
    assertStrandMetadataResult("$anon/.:0.test");
    // inside function call
    foo();
    // workers
    worker w1 {
        assertStrandMetadataResult("$anon/.:0.w1");
    }
    @strand{name:"**my strand inside worker**"}
    worker w2 {
        assertStrandMetadataResult("$anon/.:0.**my strand inside worker**");
    }

    // async function call
    future<()> f1 = start assertStrandMetadataResult("$anon/.:0.f1");

    // anonymous async call
    _ = start assertStrandMetadataResult("$anon/.:0.anon");
    _ = start assertStrandMetadataResult("$anon/.:0.anon");

    // async call with strand name
    _ = @strand{name:"**my strand**"}
            start assertStrandMetadataResult("$anon/.:0.**my strand**");

    // async function pointer
    function(string s) func = assertStrandMetadataResult;
    future<()> x = start func("$anon/.:0.x");

    // Wait until all the async calls are done
    while (successCount < (totalNoOfStrandsForTest*2) && errorCount == 0) {
        sleep(1);
    }
    if (errorCount > 0) {
        errorMessages.forEach(function(string message) {
            println(message);
        });
        panic error(ASSERTION_ERROR_REASON, message = "Test failed due to errors.");
    }
}

function foo() {
    assertStrandMetadataResult("$anon/.:0.test");
    worker w1 {
        assertStrandMetadataResult("$anon/.:0.w1");
    }
}

function assertStrandMetadataResult(string assertString) {
    string result = "";
    var env = getEnvironment();
    int id = getStrandId(env);
    var strandName = <string>java:toString(getStrandName(env));
    var module = getCurrentModule(env);
    string org = <string>java:toString(getOrg(module));
    string modName = <string>java:toString(getName(module));
    string modVersion = <string>java:toString(getMajorVersion(module));
    assertEquality(assertString, org +"/" + modName + ":" + modVersion + "." + strandName);
    assertTrue(id > 0);
}

function getEnvironment() returns handle = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Environments"
} external;

function getStrandName(handle env) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.Environment"
} external;

function getStrandId(handle env) returns int = @java:Method {
    'class: "io.ballerina.runtime.api.Environment"
} external;

function getCurrentModule(handle env) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.Environment"
} external;

function getOrg(handle module) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.Module"
} external;

function getName(handle module) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.Module"
} external;

function getMajorVersion(handle module) returns handle = @java:Method {
    'class: "io.ballerina.runtime.api.Module"
} external;

const ASSERTION_ERROR_REASON = "AssertionError";

public function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

public function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        successCount = successCount + 1;
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    errorMessages[errorCount] = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'";
    errorCount = errorCount + 1;
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
