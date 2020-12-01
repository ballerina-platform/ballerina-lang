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

public class Listener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
        return externAttach(s);
    }
}

isolated function externAttach(service object {} s) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.MockListener",
    name: "attach"
} external;

listener Listener lsn = new();
error err = error("An Error");
error diff = error("A different error");
service /hello on lsn {
    resource function get processRequest() returns error? {
        error? aDifferentError = createADifferentError();
        assertEquals(diff, aDifferentError);
        error? anotherErr = self.createError();
        assertEquals(err, anotherErr);
    }

    function createError() returns @tainted error? {
        return err;
    }
}

function createADifferentError() returns @tainted error? {
    return diff;
}

function invokeResource(string name) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.MockListener",
    name: "invokeResource"
} external;

public function testErrorFunction() {
    var err = invokeResource("$get$processRequest");
    if(err is error) {
        panic err;
    }
}

function assertEquals(anydata|error expected, anydata|error actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
