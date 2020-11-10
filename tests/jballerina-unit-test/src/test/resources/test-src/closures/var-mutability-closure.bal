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


// BASIC TYPES
function test1() returns int {
    int x = 8;
    int y = 3;
    var addFunc = function (int funcInt) returns (int) {
        x = x + 1;
        y = x + y;
        return funcInt + x + y;
    };
    int z = addFunc(7);
    return x + y + z;
}

function test2() returns int {
    int x = 2;
    var addFunc1 = function () returns (int) {
        x = x + 50;
        int y = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            y = y + 20;
            return funcInt2 + x + y;
        };
        return addFunc2(5);
    };
    int z = addFunc1();
    return x + z;
}

function test3() returns int {
    int x = 10;
    var addFunc1 = function (int funcInt1) returns (int) {
        int y = 20;
        var addFunc2 = function (int funcInt2) returns (int) {
            int z = 30 + y;
            y = 100;
            var addFunc3 = function (int funcInt3) returns (int) {
                x = x + 100;
                return funcInt3 + x + y + z;
            };
            return addFunc3(8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };
    int z = addFunc1(6);
    return x + z;
}

function test4() returns int {
    int a = 3;
    var addFunc =  function (int b) returns (int) {
        int c = 34;
        a = a + 100;
        if (b == 3) {
            int e = 34;
            c = b + e;
        }
        return b + c + a;
    };
    int z = addFunc(3);
    return a + z;
}

function getFunc1(int functionIntX) returns (function (int) returns (function (int) returns (int))) {
    int x = 100;
    return function (int functionIntY) returns (function (int) returns (int)) {
        int y = 20;
        x = x + 100;
        return function (int functionIntZ) returns (int) {
            y = y + 20;
            return functionIntX + functionIntY + functionIntZ + x + y;
        };
    };
}

function test5() returns (int){
    var getFunc2 = getFunc1(1);
    var getFunc3 = getFunc2(5);
    return getFunc3(4);
}

// TUPLES

function test6() returns [int, string] {
    string str1 = "one-";
    int x = 10;
    [int, string] res = [x, str1];
    var addFunc1 = function () returns [int, string] {
        string str2 = "three-";
        str1 = str1 + "two-" + str2;
        int y = 20;
        var addFunc2 = function () returns [int, string] {
            int z = 30 + y;
            str1 = str1 + "four-";
            var addFunc3 = function () returns [int, string] {
                str1 = str1 + "five-";
                str2 = str2 + "six";
                res = [x + y + z, str1 + str2];
                return res;
            };
            return addFunc3();
        };
        return addFunc2();
    };
    _ = addFunc1();
    return res;
}

// ARRAYS

function test7() returns string[] {
    string[] arr = ["A", "B", "C", "D"];
    var addFunc1 = function () returns string[] {
        arr[4] = "E";
        arr[5] = "H";
        var addFunc2 = function () returns string[] {
            arr[5] = "F";
            arr[6] = "G";
            var addFunc3 = function () returns string[] {
                arr[0] = "a";
                arr[2] = "e";
                arr[4] = "i";
                arr[6] = "o";
                arr[8] = "u";
                return arr;
            };
            return addFunc3();
        };
        return addFunc2();
    };
    _ = addFunc1();
    return arr;
}

// MAPS

function test8() returns map<string> {
    map<string> m1 = {a: "A", b: "B", c: "C", d: "D"};
    var addFunc1 = function () returns map<string> {
        // Add a field
        m1["e"] = "EE";
        // Update a field
        m1["a"] = "AA";
        var addFunc2 = function () returns map<string> {
            m1["b"] = "BB";
            m1["x"] = "XX";
            var addFunc3 = function () returns map<string> {
                m1["x"] = "XXXX";
                m1["a"] = "AAAA";
                m1["y"] = "YY";
                m1["z"] = "ZZ";
                return m1;
            };
            return addFunc3();
        };
        return addFunc2();
    };
    _ = addFunc1();
    return m1;
}

// OBJECTS

class Person {
    public int age;
    public string name;
    public string fullName;

    function init(int age, string firstname,  string name = "John", string lastname = "Doe") {
        self.age = age;
        self.name = name;
        self.fullName = firstname + " " + lastname;
    }
}

function test9() returns Person {
    Person p1 = new(5, "John");
    var addFunc1 = function () returns Person {
        p1.age = 10;
        p1.name = "Joe";
        var addFunc2 = function () returns Person {
            p1 = new(5, "Papadam", name = "Adam", lastname = "Page");
            var addFunc3 = function () returns Person {
                p1.age = 25;
                return p1;
            };
            return addFunc3();
        };
        return addFunc2();
    };
    _ = addFunc1();
    return p1;
}

// RECORDS

type Student record {|
    string name;
    int age;
    Grades grades;
    string...;
|};

type Grades record {
    int maths;
    int physics;
    int chemistry;
};

function test10() returns Student {
    Student stu = {name: "John Doe", age: 17, grades: {maths: 80, physics: 75, chemistry: 65}};
    var addFunc1 = function () returns Student {
        // Updated field
        stu.name = "Adam Page";
        // Added new field
        stu["email"] = "adamp@gmail.com";
        var addFunc2 = function () returns Student {
            var addFunc3 = function () returns Student {
                // Updated field
                stu["email"] = "adamp@wso2.com";
                stu["grades"]["physics"] = 100;
                // Added new field
                stu["grades"]["bio"] = 22;
                return stu;
            };
            return addFunc3();
        };
        return addFunc2();
    };
    _ = addFunc1();
    return stu;
}

// JSON

function test11() returns json {
    json price = 5.36;
    map<json> j1 = { name: "apple", color: "red", price: price };
    json resJ = {};
    var addFunc1 = function () returns json {
        var addFunc2 = function () returns json {
            j1["price"] = 12.48;
            json j2 = { name: "orange", color: "orange", price: price };
            var addFunc3 = function () returns json {
                map<json> j3 = {};
                j3["name"] = "cherry";
                var jtColor = j1.color;
                if(jtColor is json) {
                   j3["color"] = jtColor;
                }
                var j2Price = j2.price;
                if(j2Price is json) {
                  j3["price"] = j2Price;
                }

                resJ = {f1 : j1, f2 : j2, f3: j3};
                return resJ;
            };
            return addFunc3();
        };
        return addFunc2();
    };
     _ = addFunc1();
     return resJ;
}

// XML

function test12() returns xml {
    xml x1 = xml `<book>The Lost World</book>`;
    xml resX = x1;
    var addFunc1 = function () returns xml {
        xml x2 = xml `Hello, world!`;
        var addFunc2 = function () returns xml {
            x1 = xml `<book>The Princess Diaries</book>`;
            xml x3 = xml `<!--I am a comment-->`;
            var addFunc3 = function () returns xml {
                x2 =  xml `Hello, Princess!! :) `;
                resX = x1 + x2 + x3;
                return resX;
            };
            return addFunc3();
        };
        return addFunc2();
    };
     _ = addFunc1();
     return resX;
}

// ERROR

type AccountNotFoundErrorData record {
    int accountID;
    string message?;
    error cause?;
};

type AccountNotFoundError error<AccountNotFoundErrorData>;

function test13() returns AccountNotFoundError {
   string errorReason = "Account Not Found";
   AccountNotFoundError accountNotFoundError = AccountNotFoundError(errorReason, accountID = 111);

   var addFunc1 = function () returns AccountNotFoundError{
        var addFunc2 = function () returns AccountNotFoundError {
              accountNotFoundError = AccountNotFoundError(accountNotFoundError.message(), accountID = 222);
              return accountNotFoundError;
        };
        return addFunc2();
    };
    error? err = addFunc1();
    return accountNotFoundError;
}
