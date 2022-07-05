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

function test() {
    int cbp1 = 10;

    // list binding pattern
    int lbp1;
    float lbp2;
    (decimal|string)[] rbp1;
    [lbp1, lbp2, ...rbp1] = [cbp1, 12.34, 45.6d, "Hello"]; // destructuring assignment stmt
    [int, float, string...] [lbp3, lbp4, ...rbp2] = [cbp1, 23.45, "Foo", "Bar"]; // local var decl stmt
    int res1 = lbp3;
    string res2 = rbp2[0];

    // mapping binding pattern
    string mbp1;
    int mbp2;
    map<string> rbp3;
    {name: mbp1, mbp2, ...rbp3} = <Person1>{name: "Jane Doe", mbp2: 10, "foo": "bar"}; // destructuring assignment stmt
    record {string name; int age;} {name: mbp3, age, ...rbp4}
                                       = <Person2>{name: "John Doe", age: 20, "foo": "bar"}; // local var decl stmt
    res1 = age;
    res2 = <string>rbp4["foo"];
    res2 = mbp3;

    // error binding pattern
    string msg;
    error cause;
    int code;
    map<string> rbp5;
    error Error(msg, cause, code=code, ...rbp5) = error Error("FileNotFound", code = 400); // destructuring assignment stmt
    Error error Error(msg1, cause1, code=code1, ...rbp6) = error Error("FileNotFound", code = 500); // local var decl stmt
    res2 = msg1;
    res2 = rbp6["foo"];
    res1 = code1;
}

type Error error<record {|int code; string...;|}>;

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
