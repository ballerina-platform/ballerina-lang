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

# String 1
@varDecl
string s1 = "string1";

# String 2
@varDecl
configurable string s2 = "string2";

# String 3
@varDecl
final string s3 = "string3";

# String 4
@varDecl
final var s4 = "string4";

# String 5
@varDecl
isolated any s5 = s4;

# Int 1
@varDecl
isolated int i1 = 10;

# Int 2
@varDecl
configurable int i2 = ?;

# Object
@varDecl
public object {} myObj;

# Function 1
@varDecl
function (string, int) returns int func1;

# Function 2
@varDecl
isolated function () func2 = () => ();

# List
@varDecl
public [int, string, int...] [intVar, stringVar, ...otherValues] = [5, "myString", 3, 1];

# Tuple
@varDecl
[[string, int], float] [[a1, a2], a3] = [["text", 4], 89.9];

# Record
@varDecl
public record {string a; int b;} {a, b} = {a: "one", b: 2};

# Mapping
@varDecl
var {a: valueA, b: valueB} = {a: "A", b: 2};

function test() {
    @varDecl
    string str = "foo";

    @varDecl
    final var str2 = "value";
}

public annotation varDecl;
