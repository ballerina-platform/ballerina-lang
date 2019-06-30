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

type Employee record {
    string name;
    Employee? employer = ();
    int id?;
};

type Person record {
    string name;
    Person? employer = ();
    float salary;
};

function testFieldAccess1() {
    Employee e = { name: "Anne" };
    _ = e.id;
    _ = e.salary;
}

function testFieldAccess2() {
    Employee e = { name: "Anne" };
    Employee|Person ep = e;
    _ = ep.salary;
}

type EmployeeTwo record {
    string name;
    int id;
};

type PersonTwo record {
    string name;
    string id;
    float salary;
};

function testFieldAccess3() {
    PersonTwo e = { name: "s1", id: "s2", salary: 100.0 };
    EmployeeTwo|PersonTwo ep = e;
    string id = ep.id;
    int id2 = ep.id;
}
