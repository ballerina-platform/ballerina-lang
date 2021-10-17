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

type Currency record {| string x; |};

type 'record record {
    int 'float;
};

function rate(Currency 'from, string to) {
    Currency fromCurrency = 'from;
}

function 'function() returns string {
    return "Hello";
}

string a = 'function();
string 'if = "if";
string b = 'if;
string 'nonReservedVar = "The variable name is not a reserved keyword.";

function name(int x) returns int {
    return x * 10;
}

int 'any = 5;
int a = name('any);

function workers() {
    worker 'w1 {
        5 -> w2;
        int x = <- 'worker;
    }

    worker w2 {
        int i = <- w1;
    }

    worker 'worker {
        6 -> w1;
    }
}

public class Keywords {
    public string 'string;
    public int 'int;
    float 'floatNum = 0.5;

    function init(string 'foreach, int 'int) {
        self.'string = 'foreach;
        self.'int = 'int;

    }

    function getString() returns string => self.'string;

    function 'from() returns int => self.'int;
}

class 'Example {
    string 'anydata = "anydata";
    function 'foo() {}
}

function example() returns 'Example {
    'Example 'check = new();
    return 'check;
}

public function main() {
    Keywords k1 = new("abc", 123);
    k1.'int = 87;
    int 'from = k1.'from();

    'Example 'new = new;
    'new.'anydata = "new anydata";
    'new.foo();
    int \u{73}tring = 12;
    string intVal = "5"
    int 'i\u{6E}tVal2 = 0;
    int '\u{69}ntVal3 = -1;
}
