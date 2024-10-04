// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public type MetaInfo record {|
    string name;
|};

type Annot record {
    string foo;
    int bar?;
};

public const annotation MetaInfo Meta on record field;

public const annotation Annot v1 on source const, type, source worker;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation Annot v4 on parameter;
public annotation v5 on field;

public type QueryWithDifferentAnnotation record {|
    @Meta {
        name: "Potter"
    }
    string firstName;
    int age;
|};

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

@v2 {
    foo: "value 1"
}
@v2 {
    foo: "value 2"
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

function test() {
    int a = 10;

    @v1 {
        foo: "annot on worker"
    }
    worker w1 {
       a += 10;
    }
}
