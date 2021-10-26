// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

int globalVar = 5;

class Person {
    string name;
    int id;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }

    function getName() returns string {
        return self.name;
    }

    function getId() returns int {
        return self.id;
    }

    function getCar() returns Car {
        return new Car("BMW");
    }
}

class Car {
    string carName;

    function init(string carName) {
        self.carName = carName;
    }

    function getCarName() returns string {
        return self.carName;
    }   
}

type Student record {
    string firstName;
    string lastName;
    float score;
};

type Subscription record {|
    string firstName;
    string lastName;
    float score;
    string degree;
|};

public function main() {
    string name = "John";
    int number = 1234;

    Student s1 = {firstName: "Alex", lastName: "George", score: 1.5};
    Student s2 = {firstName: "Ranjan", lastName: "Fonseka", score: 0.9};

    Person person = new ("David", 789);

    int index = 1;
    string address 
    = "Colombo, Sri Lanka";

    int[] numbers = [1];

    foreach var item in numbers 
    {int z = 5; 
    }

    Student[] studentList = [s1, s2];
    stream<Student> studentStream = studentList.toStream();
    
    stream<Subscription> subscriptionStream = studentStream.filter(function (Student student) returns boolean {
        return true;
    }).'map(function (Student student) returns Subscription {
        Subscription subscription = {
            firstName: student.firstName,
            lastName: student.lastName,
            score: student.score,
            degree: "Bachelor of Medicine"
        };
        return subscription;
    });   
}
