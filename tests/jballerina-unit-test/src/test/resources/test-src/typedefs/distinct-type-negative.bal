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

import ballerina/lang.'value;
type Error0 distinct error;
type Error1 distinct error<map<value:Cloneable>>;
type Error2 distinct (Error0|Error1);

type SomeUnion distinct (Error0|Error1|record {| string key; string val; |});
type TheInt distinct int;
type TheTypeDesc distinct typedesc<int>;

type Person record {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
};

type PersonObj object {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
};

class PersonClass {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
}
