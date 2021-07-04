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

type Insn record {
    string op;
};

function testMatchClauseWithTypeGuardNegative1(Insn insn) returns string? {
    match insn {
        var {op} if op is "+" => { return "+"; }
        var {op} if op is "+" => { return "-"; }
    }
    return ();
}

const ONE = 1;
const ONE2 = 1;

function testMatchClauseWithTypeGuardNegative2() {
    int value = 1;
    match value {
        var a if a is ONE => {
        }
        var a if a is ONE => {
        }
    }
}

function testMatchClauseWithTypeGuardNegative3() {
    int value = 1;
    match value {
        var a if a is ONE2 => {
        }
        var a if a is ONE => {
        }
    }
}

function testMatchClauseWithTypeGuardNegative4() {
    int value = 1;
    match value {
        var a if a is 1|2 => {
        }
        var a if a is 1|2 => {
        }
    }
}

type A 3|4;
type B 3|4;

function testMatchClauseWithTypeGuardNegative5() {
    int value = 3;
    match value {
        var a if a is A => {
        }
        var a if a is B => {
        }
    }
}

function testMatchClauseWithTypeGuardNegative6() {
    int value = 3;
    match value {
        var a if a is int|string => {
        }
        var a if a is int|string => {
        }
    }
}

function testMatchClauseWithTypeGuardNegative7() {
    int[] value = [1, 2, 3];
    match value {
        var a if a is [1, 2, 3] => {
        }
        var a if a is [1, 2, 3] => {
        }
    }

    match value {
        var a if a is int[] => {
        }
        var a if a is int[] => {
        }
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

function testMatchClauseWithTypeGuardNegative8(Person p) {
    match p {
        {name: var a, ...var b} if a is "John" => {
        }
        {name: var a, ...var b} if a is "John" => {
        }
    }

    match p {
        var {name, age, ...rest} if age is A => {
        }
        var {name, age, ...rest} if age is B => {
        }
    }
}

type C 1|2|1;
type D 1|2;

function testMatchClauseWithTypeGuardNegative9() {
    int value = 2;
    match value {
        var a if a is C => {
        }
        var a if a is D => {
        }
    }

    match value {
        var a if a is int|string => {
        }
        var a if a is string|int|string => {
        }
    }
}
