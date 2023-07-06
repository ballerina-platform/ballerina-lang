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

    public isolated function 'start() returns error? {
    }

    public isolated function gracefulStop() returns error? {
    }

    public isolated function immediateStop() returns error? {
    }

    public isolated function detach(service object {} s) returns error? {
    }

    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return externAttach(s, name);
    }

    public function init() returns error? {
        check externLInit(self);
    }
}

isolated function externAttach(service object {} s, string[]|string? name) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "attach"
} external;

isolated function externLInit(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "listenerInit"
} external;

function getResourceAnnot(service object {} obj, string methodName, string[] path, string annotName) returns any =
@java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getResourceMethodAnnotations"
} external;

public annotation ServiceAnnot on parameter;

type Annot record {
    string val;
};

public annotation Annot PAnnot on parameter;

service class Service {
    resource function get foo(@ServiceAnnot int i) {

    }

    resource function get bar(@PAnnot { val:"annot-val-i" } int i, int j, @PAnnot { val:"annot-val-k" } string k) {

    }
}

public function testAnnot() {
    Service s = new();
    map<any> annot = <map<any>> getResourceAnnot(s, "get", ["foo"], "$param$.i");
    assertEquality(annot["ServiceAnnot"], true);

    annot = <map<any>> getResourceAnnot(s, "get", ["bar"], "$param$.i");
    assertEquality(annot["PAnnot"], <Annot> { val: "annot-val-i" });

    annot = <map<any>> getResourceAnnot(s, "get", ["bar"], "$param$.k");
    assertEquality(annot["PAnnot"], <Annot> { val: "annot-val-k" });
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
