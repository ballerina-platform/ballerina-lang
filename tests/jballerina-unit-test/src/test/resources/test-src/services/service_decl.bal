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

    public function init() returns error? {
        check externLInit(self);
        self.initialized = true;
    }
}


public class EmptyListener {

    public isolated function 'start() returns error? {
    }

    public isolated function gracefulStop() returns error? {
    }

    public isolated function immediateStop() returns error? {
    }

    public isolated function detach(service object {} s) returns error? {
    }

    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    isolated function register(service object {} s, string[]|string? name) returns error? {
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

public function callMethod(service object {} s, string name) returns future<any|error>  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name:"callMethod"
} external;

function getResourceAnnotation(string methodName, string[] funcName, string annotName) returns any = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getResourceAnnotation"
} external;

function getAnnotationsAtServiceAttach() returns map<any> = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getAnnotationsAtServiceAttach"
} external;

function getResourceAnnot(service object {} obj, string methodName, string[] path, string annotName) returns any =
@java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getResourceMethodAnnotations"
} external;


type LE EmptyListener|Listener;
listener LE lsn = new Listener();

type S service object {
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

    resource function get foo() returns service object {} {
        return service object {
            resource function get foo() returns string {
                return "foo/foo";
            }
        };
    }

    @RAnnot { val: "anot-on-dot" }  resource function get .() returns json {

    }
}

int i = 0;
function returnServiceObj() returns service object {} {
    i = i + 1;
    return service object {
        @RAnnot {val: "anot-val: " + i.toString()}  resource function get processRequest() returns json {
                return {output: "Hello"};
        }
    };
}

type MagicField object { public string magic; };

function testServiceDecl() {
    Listener l = <Listener> getListener(); // get the listener
    assertEquality(l.initialized, true);
    assertEquality(l.started, true);

    MagicField o = <MagicField> getService(); // get service attached to the listener
    assertEquality(o.magic, "The Somebody Else's Problem field");

    // Validate resource function annotation
    any val = getResourceAnnotation("get", ["processRequest"], "RAnnot");
    map<any> m = <map<any>> val;
    string s = <string> m["val"];
    assertEquality(s, "anot-val");

    val = getResourceAnnotation("get", ["."], "RAnnot");
    m = <map<any>> val;
    s = <string> m["val"];
    assertEquality(s, "anot-on-dot");

    // Test annotation on service decl
    map<any> annots = getAnnotationsAtServiceAttach();
    assertEquality(annots["ServiceAnnot"], true);

    // Create multiple services using same service constructor expression and verify that each annotations
    // is set correctly to each service object.
    service object {} s0 = returnServiceObj();
    service object {} s1 = returnServiceObj();
    service object {} s2 = returnServiceObj();
    map<any> rAnnot0  = <map<any>> getResourceAnnot(s0, "get", ["processRequest"], "RAnnot");
    assertEquality(rAnnot0["val"], "anot-val: 1");
    map<any> rAnnot1  = <map<any>> getResourceAnnot(s1, "get", ["processRequest"], "RAnnot");
    assertEquality(rAnnot1["val"], "anot-val: 2");
    map<any> rAnnot2  = <map<any>> getResourceAnnot(s2, "get", ["processRequest"], "RAnnot");
    assertEquality(rAnnot2["val"], "anot-val: 3");

    // Test service within a service
    service object {} inner = <service object {}>
        (checkpanic(wait callMethod(<service object {}> getService(), "$get$foo")));
    string str = <string> (checkpanic (wait callMethod(inner, "$get$foo")));
    assertEquality(str, "foo/foo");

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
