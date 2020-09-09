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

public function functionWithIfElse() {
    int a = 10;
    int b = 0;
    if (a == 10) {
        // do nothing
    }

    if (a < b) {
        // do nothing
    } else {
        // do nothing
    }

    if (b < 0) {
        // do nothing
    } else if (b > 0) {
        // do nothing
    } else {
        // do nothing
    }
}

public function functionWithWhile() {
    int i = 0;
    while (i < 3) {
        i = i + 1;
    }

    int j = 0;
    while (j < 5) {
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

        // do nothing
        k = k + 1;
    }
}

public function functionWithForEach() {
    string[] fruits = ["apple", "banana", "cherry"];
    foreach var v in fruits {
        // do nothing
    }

    map<string> words = {a: "apple", b: "banana", c: "cherry"};
    foreach var fruit in words {
        // do nothing
    }

    foreach var [k, v] in words.entries() {
        // do nothing
    }

    json apple = {name: "apple", colors: ["red", "green"], price: 5};
    map<json> mapValue = <map<json>>apple;
    foreach var value in mapValue {
        if (value is string) {
            // do nothing
        } else if (value is int) {
            // do nothing
        } else if (value is json[]) {
            // do nothing
        } else {
            // do nothing
        }
    }

    json[] colors = <json[]>apple.colors;
    int counter = 0;
    foreach var j in colors {
        counter += 1;
    }

    xml book = xml `<book>
                        <name>Sherlock Holmes</name>
                        <author>Sir Arthur Conan Doyle</author>
                    </book>`;
    counter = 0;
    foreach var x in book/<*> {
        counter += 1;
    }

    int endValue = 10;
    int sum = 0;
    foreach var i in 1 ... endValue {
        sum = sum + i;
    }

    // do nothing

    sum = 0;
    foreach var i in 1 ..< endValue {
        sum = sum + i;
    }
}


public function functionWithMatch() {
    int[5] intArray = [0, 1, 2, 3, 4];
    foreach var counter in intArray {
        match counter {
            0 => {
                // do nothing
            }
            1 => {
                // do nothing
            }
            2 => {
                // do nothing
            }
            3 => {
                // do nothing
            }
            4 => {
                // do nothing
            }
            5 => {
                // do nothing
            }
        }
    }

    string[] animals = ["Cat", "Canine", "Mouse", "Horse"];
    foreach string animal in animals {
        match animal {
            "Mouse" => {
                // do nothing
            }
            "Dog"|"Canine" => {
                // do nothing
            }
            "Cat"|"Feline" => {
                // do nothing
            }

            _ => {
                // do nothing
            }
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

public function functionWithTypeTest() {
    any a = "Hello, world!";
    boolean b = a is string;

    if (a is int) {
        // do nothing
    } else if (a is string) {
        // do nothing
    } else {
        // do nothing
    }

    Student alex = {name : "Alex"};
    Student|Person|Vehicle x = alex;

    if (x is Student) {
        // do nothing
    } else {
        // do nothing
    }

    if (x is Person) {
        // do nothing
    } else {
        // do nothing
    }

    if (x is Vehicle) {
        // do nothing
    } else {
        // do nothing
    }

    boolean isStudent = typeTestFunction("student") is Student;
    isStudent = typeTestFunction("vehicle") is Student;
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
    } else if (value is int) {
        value = "Hello World";

        if (value is int) {

            int i = value;
        } else {

            string|boolean sb = value;
        }
    } else {

        if (value) {
            // do nothing
        }
    }
}

function addOneToInt(int i) returns int {
    return i + 1;
}

public function functionWithElvis() {
    string? x = ();
    string output = x is string ? "value is string: " + x : "value is nil";

    string elvisOutput = x ?: "value is nil";
}
