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

type myType record { int i; } | 4 | object { int intField1; int intField2; };
type myType1 string | boolean | 1 | object { int intField1; int intField2; };

public function testAssigment() {
    testObj to = new;
    myType mt = to;
    myType1 mt1 = to;
}

class testObj {
    int intField1;
    int intField2;
    int intField = 10;
    string stringField = "";

    function objFunc() {
    }
}
