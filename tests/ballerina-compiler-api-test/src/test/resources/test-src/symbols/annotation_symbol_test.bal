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

type Annot record {
    string foo;
    int bar?;
};

public const annotation Annot v1 on source type, class, service, annotation, var, const, worker;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation Annot v4 on parameter;
public annotation v5 on return, field;

@v1 {
    foo: "annot on constant"
}
const strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    @v5 string name;
};

@v1 {
    foo: strValue
}
@v2 {
    foo: "v2 value 1"
}
@v2 {
    foo: "v2 value 2"
}
class Foo {
    @v5 string name = "ballerina";

    @v3 {
        foo: "v31 value"
    }
    public function setName(@v4 { foo: "v41 value required" } string name,
                            @v4 { foo: "v41 value defaultable" } int id = 0,
                            @v4 { foo: "v41 value rest" } string... others) returns @v5 () {
        self.name = name;
    }
}

@v3 {
    foo: "annot on function"
}
public function sum(int x, int y) returns int => x + y;

@v1 {
    foo: "annot on annotation"
}
annotation v6 on var;

type Greet service object {
    resource function get greeting() returns json;
};

@v1 {
    foo: "annot on service"
}
service Greet / on new Listener() {

    @v5
    public string msg = "Hello";

    @v3 {
        foo: "annot on resource function"
    }
    resource function get greeting() returns json => { output: self.msg };
}

function test() {
    int a = 10;

    @v1 {
        foo: "annot on worker"
    }
    worker w1 {
       a += 10;
    }
}

@v1 {
    foo: "annot on enum"
}
@v5
enum Colour {
    @v1 {
        foo: "annot on enum member"
    }
    RED,
    GREEN,
    BLUE
}

public type T2 object {
    @v5 string name;
};

// util

public class Listener {

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}
