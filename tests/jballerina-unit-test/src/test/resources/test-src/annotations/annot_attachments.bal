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

import ballerina/lang.'object as lang;

type Annot record {
    string val;
};

public annotation Annot v1 on type, class;
annotation Annot v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation Annot v5 on resource function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot v8 on service;

public const annotation map<string> v9 on source listener;
const annotation map<string> v10 on source annotation;
const annotation map<int> v11 on source var;
public const annotation map<string> v12 on source const;
const annotation map<string> v13 on source external;
const annotation map<boolean> v15 on source worker;

@v1 {
    val: "v1 value"
}
public type T1 record {
    string name;
};

@v1 {
    val: "v1 value object"
}
@v2 {
    val: "v2 value"
}
class T2 {
    string name = "ballerina";

    @v3 {
        val: "v31 value"
    }
    @v4 {
        val: 41
    }
    public function setName(@v6 { val: "v61 value required" } string name,
                            @v6 { val: "v61 value defaultable" } int id = 0,
                            @v6 { val: "v61 value rest" } string... others) returns @v7 () {
        self.name = name;
    }
}

@v3 {
    val: "v33 value"
}
public function func(@v6 { val: "v63 value required" } int id,
                     @v6 { val: "v63 value defaultable" } string s = "hello",
                     @v6 { val: "v63 value rest" } float... others) returns @v7 float {
    return 1.0;
}

@v10 {
    str: "v10 value"
}
const annotation map<string> v14 on source annotation;

@v11 {
    val: 11
}
int i = 12;

@v12 {
    str: "v12 value"
}
const F = 123.4;

@v9 {
    val: "v91"
}
listener Listener lis = new;

@v8 {
    val: "v8"
}
service ser on lis {

    @v3 {
        val: "v34"
    }
    @v5 {
        val: "54"
    }
    resource function res(@v6 { val: "v64" } int intVal) returns @v7 error? {
        return;
    }
}

service serTwo = @v8 {
                 val: "v82"
              } service {

    @v5 {
        val: "542"
    }
    resource function res(@v6 { val: "v642" } int intVal) returns @v7 () {
        return;
    }
};

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

//function externalFunction(boolean b) returns @v7 string = @v13 { strOne: "one", strTwo: "two" } external;

// Test compilation for annotations with the worker attach point.
function funcWithWorker() {

    @v15 {
        val: true
    }
    worker w1 {
        // do nothing
    }
}

function funcWithFuture() {
    future<()> fn =
    @v15 {
        val: false
    }
    start funcWithWorker();
}
