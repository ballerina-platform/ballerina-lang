// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Student record {|
   int id;
   string? fname;
   float gpa;
   int age;
   boolean feePaid;
|};

type Order record {|
    int cusId;
    int price1;
    int price2;
    string name;
    int year;
|};

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};

function getStudents() returns Student[] {
    Student res1 = {id: 1, fname: "John", gpa: 2.34, age: 14, feePaid: true};
    Student res2 = {id: 2, fname: "Monica", gpa: 0.1, age: 13, feePaid: false};
    Student res3 = {id: 3, fname: (), gpa: 4.0, age: 14, feePaid: true};
    Student res4 = {id: 4, fname: "Angie", gpa: 2.34, age: 14, feePaid: false};

    return [res1, res2, res3, res4];
}

function getOrders() returns Order[] {
    Order res1 = {cusId: 1, price1: 200, price2: 300, name: "John", year: 2001};
    Order res2 = {cusId: 2, price1: 300, price2: 200, name: "Monica", year: 2022};
    Order res3 = {cusId: 3, price1: 20, price2: 50, name: "Tom", year: 2010};
    Order res4 = {cusId: 2, price1: 10, price2: 490, name: "Monica", year: 2001};

    return [res1, res2, res3, res4];
}

function getCustomers() returns Customer[] {
    Customer res1 = {id: 1, name: "John", noOfItems: 12};
    Customer res2 = {id: 2, name: "Monica", noOfItems: 5};
    Customer res3 = {id: 3, name: "Tom", noOfItems: 25};
    Customer res4 = {id: 4, name: "Angie", noOfItems: 25};

    return [res1, res2, res3, res4];
}

function testGroupByWithVarRef() {
    Student[] studentList = getStudents();

    var res = from var {id, feePaid, age, fname, gpa} in studentList
              group by feePaid
              select fname;

    assertEquality(<string?[]>["John", "Monica", null, "Angie"], res);
}

function testGroupByWithVarDef() {
    Order[] orderList = getOrders();

    var res = from var {price1, price2, name} in orderList
              group by int cost = price1 + price2
              select name;

    assertEquality(<string[]>["John", "Monica", "Tom", "Monica"], res);
}

function testGroupByWithVarDefAndVarRef() {
    Order[] orderList = getOrders();

    var res = from var {price1, price2, name, year} in orderList
              group by int cost = price1 + price2, year
              select name;

    assertEquality(<string[]>["John", "Monica", "Tom", "Monica"], res);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
