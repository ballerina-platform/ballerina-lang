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

import ballerina/lang.'object as lang;
import ballerina/java;

type Annot record {|
   service myService;
   object {} myObject;
   handle myHandle;
   typedesc myTypedesc;
   function () myFunction;
|};

public annotation Annot v1 on type;

string strValue = "v1 value";

type Dummy object {};

class DummyImpl {
    *Dummy;
}

public function newArrayList() returns handle = @java:Constructor {
    'class:"java.util.ArrayList"
} external;

handle dummyHandle = newArrayList();

typedesc<any> T = typeof strValue;

var fn = function () {
    int x = 5;
    int y = 4;
};

DummyImpl dummyImpl = new();

@v1 {
    myService: ser,
    myObject: dummyImpl,
    myHandle: dummyHandle,
    myTypedesc: T,
    myFunction: fn
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testReadonlyTypeAnnotationAttachment()  {
    typedesc<any> t = typeof a;
    Annot? annot = t.@v1;
    assertTrue(annot is Annot);
}

listener Listener lis = new;

service ser on lis {

    resource function res(int intVal) returns () {
        return;
    }
}

class Listener {
    *lang:Listener;

    public function init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
