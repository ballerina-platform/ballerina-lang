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
    boolean initialized = false;
    boolean started = false;

    public isolated function 'start() returns error? {
        self.started = true;
        return externStart(self);
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
        return externAttach(s, name);
    }

    public function init() {
        var x = externLInit(self);
        self.initialized = true;
    }
}

isolated function externAttach(service object {} s, string[]|string? name) returns error? = @java:Method {
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

function reset() = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "reset"
} external;

function getResourceAnnotation(string funcName, string annotName) returns any = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getResourceAnnotation"
} external;

function getAnnotationsAtServiceAttach() returns map<any> = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getAnnotationsAtServiceAttach"
} external;

listener Listener lsn = new();

type S service object {
    resource function get processRequest() returns json;
};



type Annot record {
    string val;
};

public annotation Annot RAnnot on object function;
public annotation ServiceAnnot on service;

@ServiceAnnot
service S / on lsn {
    public string magic = "The Somebody Else's Problem field";

    @RAnnot { val: "anot-val" }  resource function get processRequest() returns json {
        return { output: "Hello" };
    }

    function createError() returns @tainted error? {
        return ();
    }
}

type MagicField object { public string magic; };

function testServiceDecl() {
    Listener l = <Listener> getListener(); // get the listener
    assertEquality(true, l.initialized);
    assertEquality(true, l.started);

    MagicField o = <MagicField> getService(); // get service attached to the listener
    assertEquality("The Somebody Else's Problem field", o.magic);

    // validate resource function annotation
    any val = getResourceAnnotation("$get$processRequest", "RAnnot");
    map<any> m = <map<any>> val;
    string s = <string> m["val"];
    assertEquality(s, "anot-val");

    map<any> annots = getAnnotationsAtServiceAttach();
    assertEquality(annots["ServiceAnnot"], true);

    reset();
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
