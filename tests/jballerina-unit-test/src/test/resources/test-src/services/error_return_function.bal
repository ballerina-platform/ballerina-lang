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
import ballerina/test;
import ballerina/lang.'object as lang;

public class Listener {
    *lang:Listener;
    public isolated function __start() returns error? {
    }
    public isolated function __gracefulStop() returns error? {
    }
    public isolated function __immediateStop() returns error? {
    }
    public isolated function __detach(service s) returns error? {
    }
     public isolated function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service s, string? name) returns error? {
        return externAttach(s);
    }
}

isolated function externAttach(service s) returns error? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.MockListener",
    name: "attach"
} external;

listener Listener lsn = new();
error err = error("An Error");
error diff = error("A different error");
service hello on lsn {
    resource function processRequest() returns error? {
        error? aDifferentError = createADifferentError();
        test:assertEquals(diff, aDifferentError);
        error? anotherErr = self.createError();
        test:assertEquals(err, anotherErr);
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
    var err = invokeResource("processRequest");
    if(err is error) {
        panic err;
    }
}
