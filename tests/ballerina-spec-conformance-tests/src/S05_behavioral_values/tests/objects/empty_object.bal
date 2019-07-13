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

// Objects are a combination of public and private fields along with a set of associated
// functions, called methods, that can be used to manipulate them. An object’s methods are
// associated with the object when the object is constructed and cannot be changed thereafter.

// object-type-descriptor := object-type-quals object { object-member-descriptor* }
// object-type-quals := [abstract] [client] | [client] abstract
// object-member-descriptor := object-field-descriptor | object-method | object-type-reference
type EmptyObject object {

};

@test:Config {}
function testEmptyObject() {
    EmptyObject emptyObject = new;
}
