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

function testOnConflictClauseWithNonTableTypes() {
    error onConflictError = error("Key Conflict", message = "cannot insert.");
    Person p1 = {firstName: "Alex", lastName: "George", age: 33};
    Person p2 = {firstName: "John", lastName: "David", age: 35};
    Person p3 = {firstName: "Max", lastName: "Gomaz", age: 33};
    Person[] personList = [p1, p2, p3];

    Person[] outputPersonList =
            from var person in personList
            let int newAge = 34
            where person.age == 33
            select {
                   firstName: person.firstName,
                   lastName: person.lastName,
                   age: newAge
            }
            on conflict onConflictError;
}
