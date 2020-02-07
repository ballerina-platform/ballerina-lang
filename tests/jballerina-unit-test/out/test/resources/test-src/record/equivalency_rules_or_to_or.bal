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

type Person1 record {
    string name;
    int age;
};

type AnotherPerson1 record {
    string name;
    int age;
};

function testOpenToOpenAssignment1() returns AnotherPerson1 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson1 ap = p;
    return ap;
}

type AnotherPerson2 record {
    string name;
    int|float age;
};

function testOpenToOpenAssignment2() returns AnotherPerson2 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson2 ap = p;
    return ap;
}

//////////////////////////////////////////////////////////////////
// Test for when the LHS type has optional fields which correspond to required fields of the RHS type.
// This is allowed.

type AnotherPerson4 record {
    string name;
    int age?;
};

function testReqFieldToOptField() returns AnotherPerson4 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson4 ap = p;

    return ap;
}


//////////////////////////////////////////////////////////////////
// Test for when the optional fields of RHS type correspond to optional fields of LHS type.
// This is allowed.

type Person2 record {
    string name;
    int age?;
};

function testOptFieldToOptField1() returns AnotherPerson4 {
    Person2 p = {name:"John Doe", age:25};
    AnotherPerson4 ap = p;
    return ap;
}

function testOptFieldToOptField2() returns [AnotherPerson4, anydata] {
    Person2 p = {name:"John Doe", age:25};
    AnotherPerson4 ap = p;

    p = {name:"Jane Doe"};
    AnotherPerson4 ap2 = p;

    return [ap, ap2.get("age")];
}

// Test for rest fields types.

type Foo record {|
    string s;
    string|int...;
|};

type Bar record {|
    string s;
    string|int|float...;
|};

function testRestFieldToRestField1() returns Bar {
    Foo f = {s:"qwerty", "rest1": "asdf", "rest2": 123};
    Bar b = f;
    return b;
}

function testRestFieldToRestField2() returns Bar {
    Foo f = {s:"qwerty", "rest1":"asdf", "rest2":123};
    Bar b = f;
    b["rest3"] = 23.45;
    return b;
}


//////////////////////////////////////////////////////////////////
// Test for when RHS type has additional fields than the LHS type.
// This is allowed.

type Person3 record {
    string name;
    int age;
    string address;
    float weight?;
};

function testAdditionalFieldsToRest() returns AnotherPerson1 {
    Person3 p = {name:"John Doe", age:25, address:"Colombo, Sri Lanka", weight:70.0};
    AnotherPerson1 ap = p;
    return ap;
}

public type OpenPublicPerson record {
    string name;
    int age;
    string address;
};

function testHeterogeneousTypedescEq3() returns AnotherPerson1 {
    OpenPublicPerson p = {name:"John Doe", age:25, address:"Colombo, Sri Lanka"};
    AnotherPerson1 ap = p;
    return ap;
}

function testHeterogeneousTypedescEq4() returns AnotherPerson1 {
    OpenPublicPerson op = {name:"John Doe", age:25, address:"Colombo, Sri Lanka"};
    AnotherPerson1 p = op;
    return p;
}
