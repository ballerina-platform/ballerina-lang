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

type Error error<map<string>>;

function testMissingParamsNegative() {
    error e1 = error(); // missing arg within parenthesis
    error e2 = error(2); // expected 'string', found 'int'
    error e3 = error(error("Message")); // expected 'string', found 'error'
    error e4 = error("M1", "M2"); // expected 'error?', found 'string'
    error e5 = error("M", error("M1"), "c = 2000"); // additional positional arg in error constructor

    error e6 = error Error(); // missing arg within parenthesis
    error e7 = error Error(2); // expected 'string', found 'int'
    error e8 = error Error(error("Message")); // expected 'string', found 'error'
    error e9 = error Error("M1", "M2"); // expected 'error?', found 'string'
    error e10 = error Error("M", error("M1"), "c = 2000"); // additional positional arg in error constructor
}

type MyError1 error <map<string>>;
type MyError2 error <map<string>>;
function testErrorTypeRefNegative() {
    MyError1 me1 = error MyError("Message"); // undefined error type descriptor 'MyError'
    MyError2 me2 = error MyError2("Message", c = 2); // invalid arg type in error detail field 'c', expected "
                                                     //                   "'string', found 'int'
}

type ErrorIJ error<record { int i; string j; }>;
ErrorIJ e = error("msg", i = 1, j = 2, k = error("msg"));
ErrorIJ e2 = error("msg", i = 1);

type ErrorA error;
type ErrorB error<map<string>>;

ErrorA|ErrorB u0 = error ErrorA("msg");
ErrorA|ErrorB u1 = error ErrorA("msg", a = "a");
ErrorA|ErrorB u2 = error("msg");           // cannot infer type of the error from '(error|ErrorB)'
ErrorA|ErrorB u3 = error("msg", a = "a");  // cannot infer type of the error from '(error|ErrorB)'
ErrorA|ErrorB u4 = error("msg", a = 22);

readonly|ErrorA u5 = error("msg");
readonly|ErrorA u6 = error("msg", a = "a");
readonly|ErrorA u7 = error("msg", a = 22);

type ErrorU1 MyError1|MyError2;
type ErrorU2 MyError1|map<string>;
type ErrorU3 map<int>|map<Blah>|map<string>;

error uc1 = error ErrorU1("Hello");
error uc2 = error ErrorU2("Hello");
error uc3 = error ErrorU3("Hello");

int|string uc4 = error ErrorA("msg");
int|string b = error("err");
