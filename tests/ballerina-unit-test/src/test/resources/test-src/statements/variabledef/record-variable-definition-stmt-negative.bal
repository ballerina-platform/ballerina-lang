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

type Age record {
    int age;
    string format;
};

type Person record {
    string name;
    boolean married;
};

type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};

function redeclaredSymbol() {
    Person {name: fName, married} = {name: "Peter", married: true};
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = {name: "Peter", age: {age:29, format: "Y"}, married: true, work: "SE"};
}

function bindingPatternError() {
    Person{name1: fName1, married: maritalStatus1} = {name: "John", married: true, age: 12};
    Person{name1: fName2, married: maritalStatus2} = {name1: "John", married: true, age: 12};
    Person{name: fName3, married: maritalStatus3} = {name1: "John", married: true, age: 12};
    Person{name: fName4, married: maritalStatus4, !...} = {name: "John", married: true, age: 12};
}
