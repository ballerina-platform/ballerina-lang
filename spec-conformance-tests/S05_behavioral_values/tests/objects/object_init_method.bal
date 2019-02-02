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

import ballerina/test;

const EXPECTED_OBJECT_FIELD_TO_BE_INITIALIZED_FAILURE_MESSAGE = "expected object field to have initialzed value";

// A non-abstract object type provides a way to initialize an object of the type. An object is
// initialized by:
// 1. allocating storage for the object
// 2. initializing each field with its implicit initial value, if there is one defined for the type of the field
// 3. initializing the methods of the object using the type’s method definitions
// 4. calling the object’s __init method, if there is one
type InitMethodInObject object {
    string stringField;
    float[] floatArrayField;

    public function __init() {
        self.stringField = "string field";
        self.floatArrayField = [1.0, 5.0, 10.0];
    }
};

@test:Config {}
function testInitMethodInObject() {
    InitMethodInObject obj = new;
    test:assertEquals(obj.stringField, "string field", msg = EXPECTED_OBJECT_FIELD_TO_BE_INITIALIZED_FAILURE_MESSAGE);

    float[] expArray = [1.0, 5.0, 10.0];
    test:assertEquals(obj.floatArrayField, expArray, msg = EXPECTED_OBJECT_FIELD_TO_BE_INITIALIZED_FAILURE_MESSAGE);
}
