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
};

type AnotherPerson record {
    string name;
    int age;
};

function testMissingRequiredField1() {
    Person1 p = {name:"John"};
    AnotherPerson ap = p;
}

type Person2 record {
    string name;
    int age?;
};

function testMissingRequiredField2() {
    Person2 p = {name:"John"};
    AnotherPerson ap = p;
}

type Person3 record {
    string name;
    int age;
    map<any> address;
};

function testMismatchingRestField1() {
    map<any> adr = {};
    Person3 p = {name:"John", age:25, address:adr};
    AnotherPerson ap = p;
}

type Person4 record {|
    string name;
    int age;
    any...;
|};

function testMismatchingRestField2() {
    Person4 p = {name:"John", age:25};
    AnotherPerson ap = p;
}

type AnotherPerson2 record {
    string name;
    int age;
    float weight?;
};

function testMissingOptFieldInRHS() returns AnotherPerson2 {
    Person1 p = {name:"John Doe"};
    AnotherPerson2 ap = p;
    return ap;
}
