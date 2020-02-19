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

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

Person p1 = {firstName: "Alex", lastName: "George", age: 23};
Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
Person p3 = {firstName: "John", lastName: "David", age: 33};

function testRecordInferringForMappingConstructorWithoutRestFieldNegative() {
    Person[] personList = [p1, p2, p3];

    var outputList =
            from var person in personList
            select {
                   fn: person.firstName,
                   "ln": person.lastName
            };

    string[] arr = outputList;
    outputList[0]["x"] = "foo";
}

public function testRecordInferringForMappingConstructorWithRestFieldNegative() returns record{}[] {
    Person[] personList = [p1, p2, p3];
    string key = "str";
    any a = 1;

    var outputList =
            from var person in personList
            select {
                   i: person.age,
                   [key]: a
            };

    return outputList;
}

public function testRecordInferringForMappingConstructorWithRestFieldNegative2() {
    Person[] personList = [p1, p2, p3];
    string key = "str";

    var outputList =
            from var person in personList
            select {
                   i: person.age,
                   [key]: 1,
                   b: true
            };

    var v = outputList[0];

    string s = v.i;
    int i = v.key;
    float f = v[key];
}
