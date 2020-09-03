// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type AbPersonOne client object {
    int salary;

    remote function incrementAndGetSalary(int amount) returns int;

    remote function decrementAndGetSalary(int amount) returns int;
};

public client class PersonOne {
    *AbPersonOne;

    public function init(int salary) {
        self.salary = salary;
    }

    remote function incrementAndGetSalary(int amount) returns int {
        self.salary += amount;
        return self.salary;
    }

    remote function decrementAndGetSalary(int amount) returns int {
        self.salary -= amount;
        return self.salary;
    }
}

public type AbPersonTwo client object {
    int salary;

    remote function incrementAndGetSalary(int amount) returns int;

    remote function decrementAndGetSalary(int amount) returns int;
};

public client class PersonTwo {
    *AbPersonTwo;

    public function init(int salary) {
        self.salary = salary;
    }

    remote function incrementAndGetSalary(int amount) returns int {
        self.salary += amount;
        return self.salary;
    }

    remote function decrementAndGetSalary(int amount) returns int {
        self.salary -= amount;
        return self.salary;
    }
}

function testAbstractClientObject() returns [int, int, int, int] {
    PersonOne personOne = new(10000);
    PersonTwo personTwo = new(10000);

    var result1 = personOne->incrementAndGetSalary(5000);
    var result2 = personOne->decrementAndGetSalary(2500);
    var result3 = personTwo->incrementAndGetSalary(5000);
    var result4 = personTwo->decrementAndGetSalary(2500);
    return [result1, result2, result3, result4];
}
