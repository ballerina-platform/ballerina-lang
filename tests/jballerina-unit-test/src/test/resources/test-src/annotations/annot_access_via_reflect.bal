// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/lang.'object as lang;
import ballerina/reflect;

type Annot record {
    string foo;
    int bar?;
};

type TRUE true;

public annotation map<int> v1 on function;
annotation v2 on parameter;
public annotation Annot[] v3 on return;
annotation Annot v4 on service;

listener Listener lis = new;

string v4a = "v4a";

@v4 {
    foo: v4a
}
service ser on lis {

    @v1 {
        first: 1,
        second: 2
    }
    @http:ResourceConfig {
        path: "testPath"
    }
    resource function res(@v2 int intVal, string strVal) returns
                            @v3 { foo: "v41" }  @v3 { foo: "v42", bar: 2 } () {
        return;
    }
}

function testServiceAnnotAccess() returns boolean {
    Annot? annot = <Annot?> reflect:getServiceAnnotations(ser, "v4");
    if (annot is Annot) {
        return annot.foo == v4a;
    }
    return false;
}

function testResourceAnnotAccess() returns boolean {
    map<int>? annot = <map<int>?> reflect:getResourceAnnotations(ser, "res", "v1");
    if (annot is map<int>) {
        if (annot.length() != 2 || annot["first"] != 1 || annot["second"] != 2) {
            return false;
        }
    } else {
        return false;
    }

    http:HttpResourceConfig? resourceAnnot =
            <http:HttpResourceConfig?> reflect:getResourceAnnotations(ser, "res", "ResourceConfig",
                                                                      "ballerina/http:1.0.0");
    if (resourceAnnot is http:HttpResourceConfig) {
        if (resourceAnnot.path != "testPath") {
            return false;
        }
    } else {
        return false;
    }

    // Temporary tests for parameters and return types.
    map<any> allAnnots = <map<any>> reflect:getResourceAnnotations(ser, "res", "$param$.intVal");
    TRUE? paramAnnot = <TRUE?> allAnnots["v2"];
    if (paramAnnot is ()) {
        return false;
    }

    var noAnnots = <map<any>?> reflect:getResourceAnnotations(ser, "res", "$param$.strVal");
    if !(noAnnots is ()) {
        return false;
    }

    allAnnots = <map<any>> reflect:getResourceAnnotations(ser, "res", "$returns$");
    Annot[]? annots = <Annot[]?> allAnnots["v3"];
    if (annots is Annot[]) {
        if (annots.length() != 2) {
            return false;
        }

        Annot annot1 = annots[0];
        Annot annot2 = annots[1];
        return annot1.foo == "v41" && annot2.foo == "v42" && annot2["bar"] == 2;
    }
    return false;
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
