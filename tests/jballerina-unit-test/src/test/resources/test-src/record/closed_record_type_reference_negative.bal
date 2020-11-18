// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person record {
    string name;
    int age;
};

class PersonObj {
    string name = "";
    int age = 0;
}

type Student record {|
    *PersonObj;
    string school;
|};

type IntOrFloat int|float;

type Foo1 record {|
    *IntOrFloat;
|};

type FiniteT 1|2|3|"foo"|"bar";

type Foo2 record {|
    *FiniteT;
|};

type Foo3 record {|
    *int;
    *float;
    *boolean;
    *string;
    *byte;
    *json;
    *xml;
|};

type Gender "male"|"female";

type Person2 record {
    string name;
    Gender gender;
};

type Student2 record {|
    *Person2;
    string school;
|};

function testAttributeRetainment() {
    Student2 s = {name:"John Doe", school:"ABC"};
}

type Student3 record {|
    *Person;
    *Person2;
|};

type UserData1 record {|
    *Data;
|};

type UserData2 record {|
    int index;
    *Data;
|};
