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

// binding-pattern :=
//    simple-binding-pattern
//    | structured-binding-pattern
// simple-binding-pattern := variable-name | ignore
// variable-name := identifier
// ignore := _
// structured-binding-pattern :=
//    | tuple-binding-pattern
//    | record-binding-pattern
//    | error-binding-pattern
// tuple-binding-pattern := ( binding-pattern (, binding-pattern)+ )
// record-binding-pattern := { entry-binding-patterns }
// entry-binding-patterns :=
//    field-binding-patterns [, rest-binding-pattern]
//    | [ rest-binding-pattern ]
// field-binding-patterns :=
//   field-binding-pattern (, field-binding-pattern)*
// field-binding-pattern :=
//    field-name : binding-pattern
//    | variable-name
// rest-binding-pattern := ... variable-name | ! ...
// error-binding-pattern :=
//    error ( simple-binding-pattern [, error-detail-binding-pattern] )
// error-detail-binding-pattern :=
//    simple-binding-pattern | record-binding-pattern

// TODO: Need to support error binding pattern.
@test:Config {
    groups: ["deviation"]
}
function testErrorBindingPatternBroken() {

}
