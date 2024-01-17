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


public type Person record {
    string name = "";
    int age = 0;
};

public type Employee record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

public function getEmployee2() returns Employee {
    return employee;
}

Employee employee = {
    name: person.name,
    age: person.age,
    empNo: 100
};

[Employee, Person] pp = [employee, person];

public function getEmployee() returns Employee {
    return employee;
}

public function main1() {
    var _ = getEmployee();
    var _ = getEmployee2();
}

int dep1 = dep2;
