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

function test() {
    // capture binding pattern (local var decl)
    int cbp1 = 10;
    var cbp2 = "Hello World";

    // wildcard binding pattern (destructuring assignment stmt)
    _ = 3.14;

    // list binding pattern
    int lbp1;
    float lbp2;
    (decimal|string)[] rbp1;
    [lbp1, lbp2, ...rbp1] = [10, 12.34, 45.6d, "Hello"]; // destructuring assignment stmt
    [int, float, string...] [lbp3, lbp4, ...rbp2] = [cbp1, 23.45, "Foo", "Bar"]; // local var decl stmt

    // mapping binding pattern
    string mbp1;
    int mbp2;
    map<string> rbp3;
    {name: mbp1, mbp2, ...rbp3} = <Person1>{name: "Jane Doe", mbp2: 10, "foo": "bar"}; // destructuring assignment stmt
    record {string name; int age;} {name: mbp3, age, ...rbp4}
                                       = <Person2>{name: "John Doe", age: 20, "foo": "bar"}; // local var decl stmt

    // error binding pattern
    string msg;
    error cause;
    int code;
    map<string> rbp5;
    error Error(msg, cause, code=code, ...rbp5) = error Error("FileNotFound", code = 400); // destructuring assignment stmt
    Error error Error(msg1, cause1, code=code1, ...rbp6) = error Error("FileNotFound", code = 500); // local var decl stmt
}

type Error error<record {int code;}>;

type Person1 record {|
    string name;
    int mbp2;
    string...;
|};

type Person2 record {|
    string name;
    int age;
    string...;
|};
