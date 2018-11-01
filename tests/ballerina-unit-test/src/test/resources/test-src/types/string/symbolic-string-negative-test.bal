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

type Student record {
    string name;
    int age;
};

function testWithSpaces() returns (string) {
    string var1 = 'Hello World;
    string var2 = 'Hello$;
    string var3 = 'U+001E;
    string var4 = 'Hello\nWorld;
    string var5 = 'Hello\uFFFEWorld;
    string var6 = 'Hello󰀇;
    string var6 = 'Hello󿿽;

// Maps
    map addrMap = { road: 'Mount Lavinia, country: 'SriLanka };
    addrMap['country sl] = 'PO00300;
    // Records
    Student stu = { name: 'Adam Page, age: 17};
    return stu['full$name];
    // Arrays
    string [] stringArr = ['hello, 'hello world];
    // Json
    json p = { name: 'John\uFFFFStallone };
}
