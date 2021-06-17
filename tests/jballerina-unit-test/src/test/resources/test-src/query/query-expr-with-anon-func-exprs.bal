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

type Employee record {|
    string firstName;
    string lastName;
    string dept;
|};

int y = 10;

function testQueryWithAnonFuncExprInLetClause() {
    int[] x = [1, 2];

    (function () returns int)[] fn = from int m in x
                                     let function () returns int func = function () returns int => m * y
                                     select func;
    assertEqual(fn.length(), 2);
    function () returns int f1 = fn[0];
    int value = f1();
    assertEqual(value, 10);

    f1 = fn[1];
    value = f1();
    assertEqual(value, 20);
}

function testQueryWithAnonFuncExprInWhereClause() {
    int[] x = [1, 2];

    int[] arr = from int m in x
                let function () returns int func = function () returns int => m * y
                where func === function () returns int => 20
                select m;

    assertEqual(arr.length(), 0);
}

function testQueryWithAnonFuncExprInSelectClause() {
    int[] x = [1, 2];

    (function () returns int)[] fn = from int m in x
                                     select function () returns int => m * y;

    assertEqual(fn.length(), 2);
    function () returns int f1 = fn[0];
    int value = f1();
    assertEqual(value, 10);

    f1 = fn[1];
    value = f1();
    assertEqual(value, 20);
}

function testQueryWithAnonFuncExprInSelectClause2() {
    int[] x = [1, 2];

    (function () returns int)[] fn = from int m in x
                                     select function () returns int => 0;

    assertEqual(fn.length(), 2);
    function () returns int f1 = fn[0];
    int value = f1();
    assertEqual(value, 0);

    f1 = fn[1];
    value = f1();
    assertEqual(value, 0);
}

function testNestedQueryWithAnonFuncExpr() {
    int[] x = [1, 2];
    int[] z = [10, 20];

    (function () returns int)[] fn = from int m in x
                                     from int n in z
                                     where n == m * y
                                     select function () returns int => n;

    assertEqual(fn.length(), 2);
    function () returns int f1 = fn[0];
    int value = f1();
    assertEqual(value, 10);

    f1 = fn[1];
    value = f1();
    assertEqual(value, 20);
}

Person p1 = {firstName: "Alex", lastName: "George", age: 23};
Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
Person p3 = {firstName: "John", lastName: "David", age: 33};

Person[] personList = [p1, p2, p3];

function testComplexQueryWithAnonFuncExpr() {
    (function () returns string)[] fn = from Person p in personList
                                        select function () returns string => p.firstName + " " + p.lastName;

    assertEqual(fn.length(), 3);
    function () returns string f = fn[0];
    string value = f();
    assertEqual(value, "Alex George");

    f = fn[1];
    value = f();
    assertEqual(value, "Ranjan Fonseka");

    f = fn[2];
    value = f();
    assertEqual(value, "John David");
}

function testComplexQueryWithAnonFuncExpr2() {
    (function () returns int)[] fn = from Person p in personList
                                     let int i = 10
                                     select function () returns int {
                                         return i + p.age;
                                     };

    assertEqual(fn.length(), 3);
    function () returns int f = fn[0];
    int value = f();
    assertEqual(value, 33);

    f = fn[1];
    value = f();
    assertEqual(value, 40);

    f = fn[2];
    value = f();
    assertEqual(value, 43);

    (function () returns function)[] fn2 = from Person p in personList
                                           let int i = 10
                                           select function () returns function {
                                               return function () returns int {
                                                   int v = 20;
                                                   return v + i + p.age;
                                               };
                                           };

    assertEqual(fn2.length(), 3);
    function () returns function f1 = fn2[0];
    function f2 = f1();
    int? v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 53);

    f1 = fn2[1];
    f2 = f1();
    v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 60);

    f1 = fn2[2];
    f2 = f1();
    v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 63);

    (function () returns function)[] fn3 = from Person p in personList
                                           let function () returns function func =
                                                           function () returns function {
                                                               return function () returns int {
                                                                   int n = 20;
                                                                   return n + y + p.age;
                                                               };
                                                           }
                                           select func;

    f1 = fn3[0];
    f2 = f1();
    v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 53);

    f1 = fn3[1];
    f2 = f1();
    v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 60);

    f1 = fn3[2];
    f2 = f1();
    v = f2 is (function () returns int) ? f2() : ();
    assertEqual(v is int, true);
    assertEqual(v, 63);
}

Employee e1 = {firstName: "Alex", lastName: "George", dept: "Engineering"};
Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};
Employee e3 = {firstName: "Alex", lastName: "Fonseka", dept: "Operations"};

Employee[] employeeList = [e1, e2, e3];

function testInnerQueryWithAnonFuncExpr() {
    var fn = from var person in personList
             from var empFunc in (from var emp in employeeList select function () returns string {
                 return emp.firstName + " " + emp.dept;
             })
             where person.firstName == "Alex"
             limit 2
             select function () returns string {
                 function () returns string ff = empFunc;
                 string vv = ff();
                 string aa = person.age.toString();
                 return vv + " " + aa;
             };

    assertEqual(fn.length(), 2);
    function () returns string f = fn[0];
    string details = f();
    assertEqual(details, "Alex Engineering 23");

    f = fn[1];
    details = f();
    assertEqual(details, "John HR 23");
}

int[] arr = [3, 4, 5];
(function () returns int)[] globalFn1 = from int m in arr
                                        select function () returns int => m * y;

(function () returns string)[] globalFn2 = from Person p in personList
                                           select function () returns string => p.firstName + " " + p.lastName;

int[] globalFn3 = from int m in arr
                   let function () returns int func = function () returns int => m * y
                   where func === function () returns int => 20
                   select m;

function testGlobalQueryWithAnonFuncExpr() {
    var fn1 = globalFn1;
    assertEqual(fn1.length(), 3);
    function () returns int f1 = fn1[0];
    int value = f1();
    assertEqual(value, 30);

    f1 = fn1[1];
    value = f1();
    assertEqual(value, 40);

    f1 = fn1[2];
    value = f1();
    assertEqual(value, 50);

    var fn2 = globalFn2;
    assertEqual(fn2.length(), 3);
    function () returns string f2 = fn2[0];
    string value2 = f2();
    assertEqual(value2, "Alex George");

    f2 = fn2[1];
    value2 = f2();
    assertEqual(value2, "Ranjan Fonseka");

    f2 = fn2[2];
    value2 = f2();
    assertEqual(value2, "John David");

    var fn3 = globalFn3;
    assertEqual(fn3.length(), 0);
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
