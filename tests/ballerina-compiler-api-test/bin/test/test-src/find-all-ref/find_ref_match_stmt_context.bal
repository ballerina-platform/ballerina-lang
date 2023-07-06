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

const PI = 3.14;

function test(any val) {
    match val {
        var x => {
            any a = val;
        }
        PI => {
            any b = val;
        }
        [var x, var y, ...var rest] => {
            any c = val;
            string s1 = x.toString();
            string s2 = rest.toString();
        }
        {x: var x, y: var y, ...var rest} => {
            any d = val;
            string s1 = y.toString();
            string s2 = rest.toString();
        }
        PI | "Foo" if !(val is decimal) => {
            any e = val;
        }
        _ => {
            any f = val;
        }
    } on fail error err {
        error errRef = err;
    }
}
