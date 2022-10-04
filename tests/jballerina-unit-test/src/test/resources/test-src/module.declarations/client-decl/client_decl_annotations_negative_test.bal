// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.value;

annotation A1 on client;

annotation A2 on source client;

const annotation A3 on client;

const annotation map<value:Cloneable> A4 on source client;

const annotation record {| int i; |} A5 on source client;

const annotation A6 on source client;

const annotation A7 on source var;

@A5 {
    i: 1
}
@A6
function fn() {

}


@A5 {
    i: 1
}
@A6
int modVar = 1;

@A7
@A5
@A5 {}
client "http://www.example.com/apis/one.yaml" as foo;
