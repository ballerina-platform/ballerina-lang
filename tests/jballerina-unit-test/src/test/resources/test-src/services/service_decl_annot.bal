// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class Listener {
    boolean initialized = false;
    boolean started = false;

    public isolated function 'start() returns error? {
        self.started = true;
        return externalStart(self);
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
        return externalAttach(s, name);
    }

    public function init() returns error? {
        check externalInit(self);
        self.initialized = true;
    }
}

isolated function externalAttach(service object {} s, string[]|string? name) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "attach"
} external;

isolated function externalStart(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "start"
} external;

isolated function externalInit(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "listenerInit"
} external;

function getListener() returns object {} = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "getListener"
} external;

function reset() = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "reset"
} external;

function getServiceCount() returns int = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "getServiceCount"
} external;

function getAnnotationsAtServiceAttach(int serviceNum) returns map<any> = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceAnnotValue",
    name: "getAnnotationsAtServiceAttach"
} external;

listener Listener lsn = new Listener();

type S service object {
};

type Annot record {
    string val;
};

public annotation Annot ServiceAnnot on service;

@ServiceAnnot {
    val: "service_decl_1"
}
service S /foo on lsn {
    resource function get .() returns string {
        return "Hello, World!";
    }
}

@ServiceAnnot {
    val: "service_decl_2"
}
service S /bar on lsn {
    resource function get .() returns string {
        return "Hello, World!";
    }
}

@ServiceAnnot {
    val: "service_decl_3"
}
service S /baz on lsn {
    resource function get .() returns string {
        return "Hello, World!";
    }
}

function testServiceDeclAnnots() {
    Listener l = <Listener>getListener(); // get the listener
    assertEquality(l.initialized, true);
    assertEquality(l.started, true);

    int serviceCount = getServiceCount();
    assertEquality(serviceCount, 3);

    // Test annotations on service declarations
    foreach int i in 1 ..< serviceCount {
        map<any> annots = getAnnotationsAtServiceAttach(i);
        map<any> serviceAnnot = <map<any>>annots["ServiceAnnot"];
        assertEquality(serviceAnnot["val"], "service_decl_" + i.toString());
    }

    reset();
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
