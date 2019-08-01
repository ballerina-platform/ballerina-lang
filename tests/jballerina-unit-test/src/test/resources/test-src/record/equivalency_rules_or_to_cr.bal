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

type AnotherPerson1 record {|
    string name;
    int age;
|};

function testClosedToClosedAssignment1() returns AnotherPerson1 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson1 ap = p;
    return ap;
}

type AnotherPerson2 record {|
    string name;
    int|float age;
|};

function testClosedToClosedAssignment2() returns AnotherPerson2 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson2 ap = p;
    return ap;
}

type AnotherPerson3 record {|
    string name;
    int age;
    float weight?;
|};

function testClosedToClosedAssignment3() returns AnotherPerson3 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson3 ap = p;
    return ap;
}

type AnotherPerson4 record {|
    string name;
    int age?;
|};

function testReqFieldToOptField() returns AnotherPerson4 {
    Person1 p = {name:"John Doe", age:25};
    AnotherPerson4 ap = p;
    return ap;
}

type Person2 record {
    string name;
    int age?;
};

function testOptFieldToOptField() returns AnotherPerson4 {
    Person2 p = {name:"John Doe", age:25};
    AnotherPerson4 ap = p;
    return ap;
}
