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

// A mapping is iterable as sequence of fields, where each field is represented by a 2-tuple (s,
// val) where s is a string for the name of a field, and val is the value of the field. The order
// of the fields in the sequence is implementation-dependent, but implementations are
// encouraged to preserve and use the order in which the fields were added.
@test:Config {}
function testRecordIteration() {
    // tested in `testIterableTypes()` in `05-values-types-variables/tests/values_types_variables.bal`
}
