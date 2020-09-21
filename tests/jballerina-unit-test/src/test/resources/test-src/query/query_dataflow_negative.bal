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

type Student record{|
    string firstName;
    string lastName;
    float score;
|};

type FullName record{|
	string firstName;
	string lastName;
|};

function testQueryActionWithMutableParams() returns Person[]{

    Person p1 = {firstName: "Alex", lastName: "George", age: 23};
    Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
    Person p3 = {firstName: "John", lastName: "David", age: 33};

    Person[] personList = [p1, p2, p3];

    var x = from var person in personList
            do {
                person = {firstName: "XYZ", lastName: "George", age: 30};
            };

    return personList;
}

function testReassignValueInLet() {

    Student s1 = {firstName: "Alex", lastName: "George", score: 82.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 90.6};

    Student[] studentList = [s1, s2];
	FullName[] nameList = [];

	var outputNameList =
	    from var student in studentList
	    let float twiceScore = (student.score*2)
	    do {
	        twiceScore = 1000;
	        if (twiceScore < 50) {
	            FullName fullname = {firstName:student.firstName,lastName:student.lastName};
	            nameList.push(fullname);
	        }
	    };
}
