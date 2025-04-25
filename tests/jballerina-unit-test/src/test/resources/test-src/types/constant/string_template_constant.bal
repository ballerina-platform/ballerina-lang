// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
const v1 = string `hello ${"world"}`;
const v2 = string `hello${" world"}`;

const boolean bool = true;
const v3 = string `This is ${bool}`;
const v4 = string `This is ${!bool}`;

const int intVal = 444;
string v5 = string `${intVal} is greater than ${100}.`;

const float floatVal = 5.0090;
string v6 = string `this is a float value ${floatVal}. ${v2}. Have a nice day.`;

const decimal b = 5.7888;
const decimal c = 5.7888;
const v7 = string `hello ${b + c}. This is ${b - c}.`;

public function testStringTemplateConstantExpr() {
   assertEquality("hello world", v1);
   assertEquality("hello world", v2);
   assertEquality("This is true", v3);
   assertEquality("This is false", v4);
   assertEquality("444 is greater than 100.", v5);
   assertEquality("this is a float value 5.009. hello world. Have a nice day.", v6);
   assertEquality("hello 11.5776. This is 0.", v7);

   // define a new non constant variables for the above v1 to v7 locally
   string v11 = string `hello ${"world"}`;
   string v12 = string `hello${" world"}`;
   string v13 = string `This is ${bool}`;
   string v14 = string `This is ${!bool}`;
   string v15 = string `${intVal} is greater than ${100}.`;
   string v16 = string `this is a float value ${floatVal}. ${v12}. Have a nice day.`;
   string v17 = string `hello ${b + c}. This is ${b - c}.`;

   // assert the equality of the above local variables with the constant variables
   assertEquality(v1, v11);
   assertEquality(v2, v12);
   assertEquality(v3, v13);
   assertEquality(v4, v14);
   assertEquality(v5, v15);
   assertEquality(v6, v16);
   assertEquality(v7, v17);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected '${expected.toBalString()}', found '${actual.toBalString()}'`);
}
