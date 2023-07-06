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

    (function () returns Card)[] fn4 = from var person in personList
                                       order by person.firstName descending
                                       select function () returns Card {
                                           string hello = "Hello ";
                                           function () returns string ff = function () returns string {
                                               return hello + person.firstName;
                                           };
                                           string message = ff();
                                           Card cd = {msg: message, noOfEmp: 25 + y};
                                           return cd;
                                       };

    assertEqual(fn4.length(), 3);
    function () returns Card f3 = fn4[0];
    Card card = f3();
    assertEqual("Hello Ranjan", card.msg);
    assertEqual(35, card.noOfEmp);
    f3 = fn4[1];
    card = f3();
    assertEqual("Hello John", card.msg);
    assertEqual(35, card.noOfEmp);
}

Employee e1 = {firstName: "Alex", lastName: "George", dept: "Engineering"};
Employee e2 = {firstName: "John", lastName: "David", dept: "HR"};
Employee e3 = {firstName: "Alex", lastName: "Fonseka", dept: "Operations"};

Employee[] employeeList = [e1, e2, e3];

function testInnerQueryWithAnonFuncExpr() {
    var fn1 = from var person in personList
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

    assertEqual(fn1.length(), 2);
    function () returns string f = fn1[0];
    string details = f();
    assertEqual(details, "Alex Engineering 23");

    f = fn1[1];
    details = f();
    assertEqual(details, "John HR 23");

    var fn2 = from var {firstName: fnm, lastName: lnm, age: a} in personList
              from var empFunc in (from var emp in employeeList select function () returns string {
                  return emp.dept;
              })
              where fnm == "Alex"
              limit 2
              select function () returns string {
                  function () returns string ff = empFunc;
                  string dept = ff();
                  return fnm + " " + lnm + " in " + dept;
              };

    assertEqual(fn2.length(), 2);
    f = fn2[0];
    details = f();
    assertEqual(details, "Alex George in Engineering");

    f = fn2[1];
    details = f();
    assertEqual(details, "Alex George in HR");
}

function testQueryWithNestedLambdaFunctions() {
    var fn1 = from var {firstName: fnm, lastName: lnm, age: a} in personList
              select function () returns (function() returns (function () returns string)) {
                  return function() returns (function () returns string) {
                      return function () returns string => fnm + " " + lnm;
                  };
              };

    assertEqual(3, fn1.length());
    function () returns (function() returns (function () returns string)) f1 = fn1[0];
    function() returns (function () returns string) f2 = f1();
    function () returns string f3 = f2();
    string v = f3();
    assertEqual(v, "Alex George");

    f1 = fn1[1];
    f2 = f1();
    f3 = f2();
    v = f3();
    assertEqual(v, "Ranjan Fonseka");

    f1 = fn1[2];
    f2 = f1();
    f3 = f2();
    v = f3();
    assertEqual(v, "John David");

    var fn2 = from int i in [1, 2, 3, 4]
              let function () returns (function () returns (int)) func = function () returns
                                                           (function () returns (int)) => foo
              select function () returns int {
                  function () returns (int) ff = func();
                  int val = ff();
                  return val + i;
              };

    assertEqual(4, fn2.length());
    function () returns int f4 = fn2[0];
    int sum = f4();
    assertEqual(sum, 4);

    f4 = fn2[1];
    sum = f4();
    assertEqual(sum, 5);

    f4 = fn2[2];
    sum = f4();
    assertEqual(sum, 6);

    f4 = fn2[3];
    sum = f4();
    assertEqual(sum, 7);

    var fn3 = from var {firstName: fnm, lastName: lnm, age: a} in personList
              select function () returns string {
                  function (string, string) returns (string) anonFunction =
                  function (string x, string y) returns (string) {
                      return x + " " + y;
                  };
                  return anonFunction(fnm, lnm);
             };

    assertEqual(3, fn3.length());
    function () returns string f5 = fn3[0];
    string nm = f5();
    assertEqual(nm, "Alex George");

    f5 = fn3[1];
    nm = f5();
    assertEqual(nm, "Ranjan Fonseka");

    f5 = fn3[2];
    nm = f5();
    assertEqual(nm, "John David");

    var fn4 = from var empFunc in (from var {firstName, lastName, dept} in employeeList
                                   select function () returns string {
                                       var grades = from var i in ["A", "B", "C"]
                                                    select i;
                                       return firstName + "'s grades:" + grades.toString();
                                   })
              select empFunc;

    assertEqual(3, fn4.length());
    function () returns string f6 = fn4[0];
    string empGrades = f6();
    assertEqual(empGrades, "Alex's grades:[\"A\",\"B\",\"C\"]");

    f6 = fn4[1];
    empGrades = f6();
    assertEqual(empGrades, "John's grades:[\"A\",\"B\",\"C\"]");

    f6 = fn4[2];
    empGrades = f6();
    assertEqual(empGrades, "Alex's grades:[\"A\",\"B\",\"C\"]");
}

function foo() returns int {
    return 1 + 2;
}

int[] arr = [3, 4, 5];
(function () returns int)[] globalFn1 = from int m in arr
                                        select function () returns int => m * y;

(function () returns string)[] globalFn2 = from Person p in personList
                                           select function () returns string => p.firstName + " " + p.lastName;

int[] globalFn3 = from int m in arr
                  let function () returns int func = function () returns int => m * y
                  where func === function () returns int {
                      return 20;
                  }
                  select m;

(function () returns string)[] globalFn4 = from var {firstName, lastName, age} in personList
                                           where firstName == "Alex"
                                           select function () returns string {
                                               string hello = "Hello ";
                                               return hello + firstName + " " + lastName;
                                           };

type Card record {|
   string msg;
   int noOfEmp;
|};

(function () returns Card)[] globalFn5 = from var person in personList
                                         from var empFunc in (from var {firstName, lastName, dept} in employeeList
                                         select function () returns string => firstName + " " + dept)
                                         where person.firstName == "Alex"
                                         limit 2
                                         select function () returns Card {
                                             function () returns string ff = empFunc;
                                             string message = ff();
                                             Card cd = {msg: message, noOfEmp: 25 + y};
                                             return cd;
                                         };

var globalFn6 = from var person in personList
                select function () returns (function() returns (function () returns string)) {
                    return function() returns (function () returns string) {
                        return function () returns string => person.firstName + " " + person.lastName;
                    };
                };

(function () returns string)[] globalFn7 = messageFunc1("John");
string hello = "Hello ";

function messageFunc1(string name) returns (function () returns string)[] {
    var res = from var empFunc in (from var {firstName, lastName, dept} in employeeList
              select function () returns string => hello + name + " in " + dept)
              select empFunc;

    return res;
}

(function () returns string)[] globalFn8 = messageFunc2("John");

function messageFunc2(string name) returns (function () returns string)[] {
    var res = from var person in personList
              from var {firstName, lastName, dept} in employeeList
              order by firstName, dept descending
              limit 4
              select function () returns string => hello + name + " in " + dept;

    return res;
}

var globalFn9 = from var empFunc in (from var {firstName, lastName, dept} in employeeList
                                     select function () returns string {
                                         var grades = from var i in ["A", "B", "C"]
                                                      select i;
                                         return firstName + "'s grades:" + grades.toString();
                                     })
                select empFunc;

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

    var fn4 = globalFn4;
    assertEqual(fn4.length(), 1);
    function () returns string f4 = fn4[0];
    string value3 = f4();
    assertEqual(value3, "Hello Alex George");

    var fn5 = globalFn5;
    assertEqual(fn5.length(), 2);
    function () returns Card f5 = fn5[0];
    Card card1 = f5();
    assertEqual("Alex Engineering", card1.msg);
    assertEqual(35, card1.noOfEmp);
    f5 = fn5[1];
    card1 = f5();
    assertEqual("John HR", card1.msg);
    assertEqual(35, card1.noOfEmp);

    assertEqual(3, globalFn6.length());
    function () returns (function() returns (function () returns string)) f6 = globalFn6[0];
    function() returns (function () returns string) f6a = f6();
    function () returns string f6b = f6a();
    string personName = f6b();
    assertEqual(personName, "Alex George");

    f6 = globalFn6[1];
    f6a = f6();
    f6b = f6a();
    personName = f6b();
    assertEqual(personName, "Ranjan Fonseka");

    f6 = globalFn6[2];
    f6a = f6();
    f6b = f6a();
    personName = f6b();
    assertEqual(personName, "John David");

    assertEqual(3, globalFn7.length());
    function () returns string f7 = globalFn7[0];
    string msg = f7();
    assertEqual(msg, "Hello John in Engineering");

    f7 = globalFn7[1];
    msg = f7();
    assertEqual(msg, "Hello John in HR");

    f7 = globalFn7[2];
    msg = f7();
    assertEqual(msg, "Hello John in Operations");

    assertEqual(4, globalFn8.length());
    f7 = globalFn8[0];
    msg = f7();
    assertEqual(msg, "Hello John in Operations");

    f7 = globalFn8[1];
    msg = f7();
    assertEqual(msg, "Hello John in Operations");

    f7 = globalFn8[2];
    msg = f7();
    assertEqual(msg, "Hello John in Operations");

    f7 = globalFn8[3];
    msg = f7();
    assertEqual(msg, "Hello John in Engineering");

    assertEqual(3, globalFn9.length());
    function () returns string f8 = globalFn9[0];
    string empGrades = f8();
    assertEqual(empGrades, "Alex's grades:[\"A\",\"B\",\"C\"]");

    f8 = globalFn9[1];
    empGrades = f8();
    assertEqual(empGrades, "John's grades:[\"A\",\"B\",\"C\"]");

    f8 = globalFn9[2];
    empGrades = f8();
    assertEqual(empGrades, "Alex's grades:[\"A\",\"B\",\"C\"]");
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
