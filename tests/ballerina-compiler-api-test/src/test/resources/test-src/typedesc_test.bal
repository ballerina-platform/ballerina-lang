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

const PI = 3.14;

type PathConfig record {|
    string path;
|};

public const annotation PathConfig Path on source const;

function foo(int x, float y = 12.34, int... rest) returns int {
    return 0;
}

class PersonObj {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}

function bar() {
    decimal sum = 10.1 + 20.3;
}

function test() {
    int x = foo(10, 3.4, 10);

    future<int> f1 = start foo(20, 4.5);

    PersonObj[] arr = [];

    map<string> m1 = {};

    [int, string, float...] tup = [];

    typedesc<anydata> td = int;
    typedesc td2 = string;

    int|string|float union = 10;

    Number n = 20;

    Digit d = 1;

    Format fmt = DEFAULT;
}

type Number int|float|decimal;

type Digit 0|1|2|3;

public type Format DEFAULT|CSV|TDF;

public const DEFAULT = "default";
public const CSV = "csv";
public const TDF = "tdf";
