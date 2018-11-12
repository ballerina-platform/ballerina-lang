// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testStaticMatchPatternsBasic1() returns string[] {
    string | int | boolean a1 =  12;
    string | int | boolean a2 =  "Hello";
    string | int | boolean a3 =  true;

    string | int | boolean a4 =  15;
    string | int | boolean a5 =  "HelloAgain";
    string | int | boolean a6 =  false;

    string | int | boolean a7 =  "NothingToMatch";

    string[] result = [foo(a1), foo(a2), foo(a3), foo(a4), foo(a5), foo(a6), foo(a7)];

    return result;
}

function foo(string | int | boolean a) returns string {
    match a {
        12 => return "Value is '12'";
        "Hello" => return "Value is 'Hello'";
        15 => return "Value is '15'";
        true => return "Value is 'true'";
        false => return "Value is 'false'";
        "HelloAgain" => return "Value is 'HelloAgain'";
    }

    return "Value is 'Default'";
}


function testStaticMatchPatternsBasic2() returns string[] {
    string | int | boolean a1 =  12;
    string | int | boolean a2 =  "Hello";

    string | int | boolean a3 =  15;
    string | int | boolean a4 =  "HelloWorld";

    string | int | boolean a5 =  "HelloAgain";
    string | int | boolean a6 =  34;

    string | int | boolean a7 =  "NothingToMatch";
    string | int | boolean a8 =  false;

    string | int | boolean a9 =  15;
    string | int | boolean a10 =  34;

    string | int | boolean a11 =  true;
    string | int | boolean a12 =  false;

    string[] result = [bar(a1, a2), bar(a3, a4), bar(a5, a6), bar(a7, a8), bar(a9, a10), bar(a11, a12)];

    return result;
}

function bar(string | int | boolean a, string | int | boolean b)  returns string{
    match a {
        12 => return "Value is '12'";
        "Hello" => return "Value is 'Hello'";
        15 => match b {
                 34 => return "Value is '15 & 34'";
                 "HelloWorld" => return "Value is '15 & HelloWorld'";
              }
        "HelloAgain" => match b {
                           34 => return "Value is 'HelloAgain & 34'";
                           "HelloWorld" => return "Value is 'HelloAgain & HelloWorld'";
                        }
        true => return "Value is 'true'";
    }

    return "Value is 'Default'";
}
