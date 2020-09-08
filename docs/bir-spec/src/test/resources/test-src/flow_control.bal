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

import ballerina/io;

public function functionWithIfElse() {
    int a = 10;
    int b = 0;
    if (a == 10) {
        io:println("a == 10");
    }

    if (a < b) {
        io:println("a < b");
    } else {
        io:println("a >= b");
    }

    if (b < 0) {
        io:println("b < 0");
    } else if (b > 0) {
        io:println("b > 0");
    } else {
        io:println("b == 0");
    }
}

public function functionWithWhile() {
    int i = 0;
    while (i < 3) {
        io:println(i);
        i = i + 1;
    }

    int j = 0;
    while (j < 5) {
        io:println(j);
        j = j + 1;
        if (j == 3) {
            break;
        }
    }

    int k = 0;
    while (k < 5) {
        if (k < 3) {
            k = k + 1;
            continue;
        }

        io:println(k);
        k = k + 1;
    }
}

public function functionWithForEach() {
    io:println("Iterating a string array :");
    string[] fruits = ["apple", "banana", "cherry"];
    foreach var v in fruits {
        io:println("fruit: ", v);
    }

    io:println("\nIterating a map :");
    map<string> words = {a: "apple", b: "banana", c: "cherry"};
    foreach var fruit in words {
        io:println(fruit);
    }

    foreach var [k, v] in words.entries() {
        io:println("letter: ", k, ", word: ", v);
    }

    io:println("\nIterating a JSON object :");
    json apple = {name: "apple", colors: ["red", "green"], price: 5};
    map<json> mapValue = <map<json>>apple;
    foreach var value in mapValue {
        if (value is string) {
            io:println("string value: ", value);
        } else if (value is int) {
            io:println("int value: ", value);
        } else if (value is json[]) {
            io:println("json array value: ", value);
        } else {

            io:println("non-string value: ", value);
        }
    }

    io:println("\nIterating a JSON array :");
    json[] colors = <json[]>apple.colors;
    int counter = 0;
    foreach var j in colors {
        io:println("color ", counter, ": ", j);
        counter += 1;
    }

    io:println("\nIterating XML :");
    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    counter = 0;
    foreach var x in book/<*> {
        io:println("xml at ", counter, ": ", x);
        counter += 1;
    }

    io:println("\nIterating a closed integer range :");
    int endValue = 10;
    int sum = 0;
    foreach var i in 1 ... endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to ", endValue, " is ", sum);

    io:println("\nIterating a half open integer range :");
    sum = 0;
    foreach var i in 1 ..< endValue {
        sum = sum + i;
    }
    io:println("summation from 1 to ", endValue,
                        " excluding ", endValue, " is ", sum);
}


public function functionWithMatch() {
    int[5] intArray = [0, 1, 2, 3, 4];
    foreach var counter in intArray {
        match counter {
            0 => {
                io:println("value is: 0");
            }
            1 => {
                io:println("value is: 1");
            }
            2 => {
                io:println("value is: 2");
            }
            3 => {
                io:println("value is: 3");
            }
            4 => {
                io:println("value is: 4");
            }
            5 => {
                io:println("value is: 5");
            }
        }
    }

    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];
    foreach string animal in animals {
        match animal {
            "Mouse" => {
                io:println("Mouse");
            }
            "Dog"|"Canine" => {
                io:println("Dog");
            }
            "Cat"|"Feline" => {
                io:println("Cat");
            }

            _ => {
                io:println("Match All");
            }
        }
    }
}


public function functionWithTupleMatch() {
    [string, int]|[float, string, boolean]|float a1 = 66.6;
    [string, int]|[float, string, boolean]|float a2 = ["Hello", 12];
    [float, boolean]|[float, string, boolean]|float a3 = [4.5, true];
    [string, int]|[float, string, boolean]|float a4 = [6.7, "Test", false];

    basicTupleMatch(a1);
    basicTupleMatch(a2);
    basicTupleMatch(a3);
    basicTupleMatch(a4);
    [string, int]|[boolean, int]|[int, boolean]|int|float b1 = ["Hello", 45];
    [string, int]|[float, boolean]|[int, boolean]|int|float b2 = [4.5, true];
    [float, boolean]|[boolean, int]|[int, boolean]|int|float b3 = [false, 4];
    [string, int]|[int, boolean]|int|float b4 = [455, true];
    [float, boolean]|[boolean, int]|[int, boolean]|float b5 = 5.6;

    matchWithMatchGuard(b1);
    matchWithMatchGuard(b2);
    matchWithMatchGuard(b3);
    matchWithMatchGuard(b4);
    matchWithMatchGuard(b5);
}

function basicTupleMatch(any a) {
    match a {

        var [s, i, b] => {
            io:println("Matched with three vars : ", io:sprintf("%s", a));
        }

        var [s, i] => {
            io:println("Matched with two vars : ", io:sprintf("%s", a));
        }

        var s => {
            io:println("Matched with single var : ", io:sprintf("%s", a));
        }
    }
}

function matchWithMatchGuard(any b) {
    match b {

        var [s, i] if (s is string && i is int) => {
            io:println("'s' is string and 'i' is int : ", io:sprintf("%s", b));
        }

        var [s, i] if s is float => {
            io:println("Only 's' is float : ", io:sprintf("%s", b));
        }

        var [s, i] if i is int => {
            io:println("Only 'i' is int : ", io:sprintf("%s", b));
        }

        var [s, i] => {
            io:println("No type guard : ", io:sprintf("%s", b));
        }

        var s if s is float => {
            io:println("'s' is float only : ", io:sprintf("%s", b));
        }

    }
}

type RecordOne record {
    string var1;
};

type RecordTwo record {|
    string var1;
    int var2;
|};

type RecordThree record {
    string var1;
    RecordTwo var2;
};


public function functionWithRecordMatch() {
    RecordOne rec1 = {var1: "Hello", "var2": 150};
    RecordOne rec2 = {var1: "Hello", "var2": true};
    RecordOne rec3 = {var1: "Hello", "var2": 150, "var3": true};
    RecordOne rec4 = {var1: "Hello"};
    basicRecordMatch(rec1);
    basicRecordMatch(rec2);
    basicRecordMatch(rec3);
    basicRecordMatch(rec4);
    RecordTwo tRec1 = {var1: "Ballerina", var2: 500};
    RecordThree tRec2 = {var1: "Language", var2: tRec1};
    matchWithTypeGuard(tRec1);
    matchWithTypeGuard(tRec2);
    matchWithTypeGuard(true);
}

function basicRecordMatch(any a) {
    match a {

        var {var1, var2, var3} => {
            io:println("Matched with three vars : ", var1, ", ", var2, ", ", var3);
        }

        var {var1, var2} => {
            io:println("Matched with two vars : ", var1, ", ", var2);
        }

        var {var1} => {
            io:println("Matched with single var : ", var1);
        }
    }
}

function matchWithTypeGuard(any matchExpr) {

    match matchExpr {

        var {var1, var2} if var2 is string => {
            io:println("Matched with string typeguard");
        }

        var {var1, var2} if (var1 is int && var2 is int) => {
            io:println("Matched with int and int typeguard : ", var1);
        }

        var {var1, var2} if (var1 is string && var2 is int) => {
            io:println("Matched with string and int typeguard : ", var1);
        }

        var {var1, var2} if (var1 is int && var2 is RecordTwo) => {
            io:println("Matched with int and RecordTwo typeguard : ", var1);
        }

        var {var1, var2} if (var1 is string && var2 is RecordTwo) => {
            io:println("Matched with string and RecordTwo typeguard : ", var2.var1);
        }

        var x => {
            io:println("Matched with Default");
        }
    }
}

public function functionWithTypeTest() {
    any a = "Hello, world!";
    boolean b = a is string;
    io:println("Is 'a' a string? ", b);

    if (a is int) {
        io:println("'a' is an int with value: ", a);
    } else if (a is string) {
        io:println("'a' is a string with value: ", a);
    } else {
        io:println("'a' is not an int or string, with value: ", a);
    }

    Student alex = {name : "Alex"};
    Student|Person|Vehicle x = alex;

    if (x is Student) {
        io:println("Alex is a student");
    } else {
        io:println("Alex is not a student");
    }

    if (x is Person) {
        io:println("Alex is a person");
    } else {
        io:println("Alex is not a person");
    }

    if (x is Vehicle) {
        io:println("Alex is a vehicle");
    } else {
        io:println("Alex is not a vehicle");
    }

    boolean isStudent = typeTestFunction("student") is Student;
    io:println("Does foo return a student? ", isStudent);
    isStudent = typeTestFunction("vehicle") is Student;
    io:println("Does foo return a student? ", isStudent);
}

type Person record {
    string name;
};

type Student record {
    string name;
    int age = 0;
};

type Vehicle record {
    string brand;
};

function typeTestFunction(string t) returns any {
    if (t == "student") {
        return <Student>{name: "Alex"};
    } else if (t == "vehicle") {
        return <Vehicle>{brand: "Honda"};
    }
    return "invalid type";
}


public function functionWithTypeGuard() {
    string|int|boolean value = 10;

    if (value is string) {

        string str = value;
        io:println("value is a string: ", str);
    } else if (value is int) {

        io:println("value is an int: ", value);
        io:println("value + 1: ", addOneToInt(value));

        value = "Hello World";

        if (value is int) {

            int i = value;
            io:println("- value is an int: ", i);
        } else {

            string|boolean sb = value;
            io:println("- value is string|boolean: ", sb);
        }
    } else {

        if (value) {
            io:println("s is 'true'");
        }
    }
}

function addOneToInt(int i) returns int {
    return i + 1;
}

public function functionWithElvis() {
    string? x = ();
    string output = x is string ? "value is string: " + x : "value is nil";
    io:println("The output from the type-guard: " + output);

    string elvisOutput = x ?: "value is nil";
    io:println("The output from the elvis operator: " + elvisOutput);
}
