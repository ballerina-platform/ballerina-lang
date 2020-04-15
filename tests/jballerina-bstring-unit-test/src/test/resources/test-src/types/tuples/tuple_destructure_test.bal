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

type Person record {
    string name;
};

type Employee record {
    string name;
    boolean intern;
};

Person person1 = { name: "Person 1" };
Employee employee1 = { name: "Employee 1", intern: true };
Employee employee2 = { name: "Employee 2", intern: false };

function tupleDestructureTest1() returns [int, string] {
	[int, string] x = [1, "a"];
    int a;
    string b;
    [a, b] = x;
    return [a, b];
}

function tupleDestructureTest2() returns [int, int[]] {
    [int, int, int, int] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest3() returns [int, int[]] {
    [int, int, int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest4() returns int[] {
    [int...] x = [1, 2, 3, 4];
    int[] a;
    [...a] = x;
    return a;
}

function tupleDestructureTest5() returns [int, int[]] {
    [int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest6() returns [int, string[]] {
    [int, string, string] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest7() returns [int, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest8() returns [int, string, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string b;
    string[] c;
    [a, b, ...c] = x;
    return [a, b, c];
}

type FooRecord record {|
    string field1;
    [int, float] field2;
|};

function tupleDestructureTest9() returns [boolean, string, [int, float]] {
    FooRecord foo = {
        field1: "string value",
        field2: [25, 12.5]
    };

    boolean a;
    string b;
    [int, float] c;
    [a, { field1: b, field2: c }] = [true, foo];

    return [a, b, c];
}
