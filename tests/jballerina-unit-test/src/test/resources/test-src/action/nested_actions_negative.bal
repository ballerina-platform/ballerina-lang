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

client class Foo1 {
    string name;

    function init(string name) {
        self.name = name;
    }

    remote function getName(string s = "") returns string {
        return s == "" ? (self.name) : self.name + "->" + s;
    }
}

function testNestedClientObjectActions() {
    Foo1 f1 = new("f1");
    Foo1 f2 = new("f2");
    Foo1 f3 = new("f3");

    // actions cannot be used as arguments
    string result = f1->getName(f2->getName(f3->getName()));
}
