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

import testorg/foo;

function f1() {
    record {| 1 a; |} _ = foo:ECONST; // error incompatible types: expected 'record {| 1 a; |}', found '(record {| 1 a; 2 b; |} & readonly)'
    readonly & record {| record {| 1 a; 2 b; |} a; record {| 3 a; |} b; |} _ = foo:FCONST; // error incompatible types: expected 'record {| readonly (record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 3 a; |} & readonly) b; |} & readonly', found '(record {| record {| 1 a; 2 b; |} a; record {| 1 a; |} b; |} & readonly)'
}

function f2() {
    var a = foo:ECONST;
    a.a = 1; // error cannot update 'readonly' value of type 'record {| readonly 1 a; readonly 2 b; |} & readonly'
    a.b = 1; // error cannot update 'readonly' value of type 'record {| readonly 1 a; readonly 2 b; |} & readonly'

    var b = foo:FCONST;
    b.a.a = 2; // error cannot update 'readonly' value of type 'record {| readonly (record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 1 a; |} & readonly) b; |} & readonly'
    b.b.a = 2; // error cannot update 'readonly' value of type 'record {| readonly (record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 1 a; |} & readonly) b; |} & readonly'
    b.b = {}; // error cannot update 'readonly' value of type 'record {| readonly (record {| 1 a; 2 b; |} & readonly) a; readonly (record {| 1 a; |} & readonly) b; |} & readonly'
}
