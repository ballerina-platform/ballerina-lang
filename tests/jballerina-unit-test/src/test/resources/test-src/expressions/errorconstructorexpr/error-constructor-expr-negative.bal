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

type Error error<*>;

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

type MyError1 error <*>;
type MyError2 error <map<string>>;
function testErrorTypeRefNegative() {
    MyError1 me1 = error MyError("Message"); // undefined error type descriptor 'MyError'
    MyError2 me2 = error MyError2("Message", c = 2); // invalid arg type in error detail field 'c', expected "
                                                     //                   "'string', found 'int'
}
