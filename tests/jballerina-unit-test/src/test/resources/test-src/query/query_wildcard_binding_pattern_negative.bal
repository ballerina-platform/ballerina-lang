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

function testWildcardBindingPatternInQueryExprNegative() {
    error?[] x = [];

    int[] _ = from error? _ in x
        select 1;

    error[] y = [];

    int[] _ = from var _ in y
        select 1;
}

function testWildcardBindingPatternInQueryActionNegative() {
    error?[] x = [];

    error? res = from error? _ in x
        do {
        };

    error[] y = [];

    res = from var _ in y
        do {
        };

    boolean _ = res is (); // to avoid unused variable warning
}
