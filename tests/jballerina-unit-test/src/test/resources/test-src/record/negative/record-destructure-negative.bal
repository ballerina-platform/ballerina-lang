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

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

public function main() {
    string g;
    string h;
    int i;
    {firstName:g, lastName:h, _} = fooPerson(1, false);
}

function fooPerson(float a, boolean b) returns Person {
    return {firstName:"john", lastName:"williams", age:40};
}

type EmployeeOne record {
    int a;
    record {
        int b?;
    }[1] age;
};

type EmployeeTwo record {
    int age;
    map<string> name;
};

function testInvalidFieldBindingPattern(){
    EmployeeOne emp = {a: 4, age: [{b: 5}]};
    int empOneName;
    int empOneAge;
    {a: empOneName, age: [{b: empOneAge}]} = emp;

    EmployeeTwo empTwo = {age: 4, name: {first:"Joe"}};
    int empAge;
    string empName;
    {age:empAge, name:{first:empName}} = empTwo;
}
