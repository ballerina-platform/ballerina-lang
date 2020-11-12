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
import testorg/serv_classes as serv;

public class Listener {
    boolean initialized = false;
    boolean started = false;

    public isolated function __start() returns error? {
        self.started = true;
        return externStart(self);
    }
    public isolated function __gracefulStop() returns error? {
    }
    public isolated function __immediateStop() returns error? {
    }
    public isolated function __detach(service object {} s) returns error? {
    }
     public isolated function __attach(service object {} s, string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service object {} s, string? name) returns error? {
        return externAttach(s);
    }

    public function init() {
        var x = externLInit(self);
        self.initialized = true;
    }
}

isolated function externAttach(service object {} s) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "attach"
} external;

isolated function externStart(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "start"
} external;

isolated function externLInit(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "listenerInit"
} external;

function getListener() returns object {} = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getListener"
} external;

function getService() returns object {} = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getService"
} external;

listener Listener lsn = new();

function testServiceObjectValue() {
    serv:Service serviceVal = new();

    var e = lsn.__attach(serviceVal);

    Listener l = <Listener> getListener(); // get the listener
    assertEquality(true, l.initialized);
    assertEquality(true, l.started);

    var x = wait callMethod(serviceVal, "$get$bar");
    assertEquality(x, "bar");

    var y = wait callMethod(serviceVal, "$put$bar");
    assertEquality(y, "put-bar");
}


public function callMethod(service object {} s, string name) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethod"
} external;


function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
