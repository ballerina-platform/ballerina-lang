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

enum Language {
    ENG = "English",
    TL = "Tamil",
    SI = "Sinhala"
}

public class Counter {
    private int n;

    public function init(int n = 0) {
        self.n = n;
    }

    public function get() returns int {
        return self.n;
    }

    public function inc() {
        self.n += 1;
    }
}

type Headers record {
    string 'from;
    string to;
    string subject?;
};

Headers h = {
    'from: "John",
    to: "Jill"
};

public function main() {
    Counter ct = new();
    Counter ct2 = new();
    int v01_intVar = 10;
    int v02_intVar = 1;
    int v03_intVar = 5;
    int z = 0;

    // if statement
    if (v02_intVar < 0 && v03_intVar == 5) {
        z = 4;
    } else if (v02_intVar > 0 && v03_intVar == 5) {
        z = 5;
    } else {
        z = 6;
    }

    // while loop
    int v04_intVar = 0;
    while (true) {
        if (v04_intVar >= 1) {
            break;
        }
        v04_intVar = v04_intVar + 1;
        z += 1;
    }

    // foreach loop
    string[] v05_fruits = ["apple", "orange"];
    foreach string v in v05_fruits {
        string fruit = v;
        z += 1;
    }

    // match statement
    int[1] v06_intArray = [7];
    int v07_intVar;
    foreach var counter in v06_intArray {
        match counter {
            7 => {
                v07_intVar = 7;
            }
        }
    }
}

# Adds two integers.
# + x - an integer
# + y - another integer
# + return - the sum of `x` and `y`
public function add(int x, int y) returns int {
    return x + y;
}

function altFetch(string urlA, string urlB) returns int|error {

    worker A returns int|error {
        return add(1, 2);
    }

    worker B returns int|error {
        return add(3, 4);
    }

    return wait A | B;
}
