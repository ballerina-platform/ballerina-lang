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

function testQueryingEmptyTuple() {
    var _ = from var user in []
        select user;

    any[] x1 = from var user in []
        select user;

    var _ = from var user in []
        select user.cloneReadOnly();

    string[] x2 = from var user in []
        select user.toString();

    [never...] arr = [];
    var _ = from var user in arr
            select user;

    var arr1 = [];
    var _ = from var x in arr1
        select x.clone();

    var _ = from var user in (from var x in []
            select x)
        select user;

    var _ = from var user in [[]]
        select (from var e in user
            select e);

    error? err = from var item in []
        do {
            _ = item;
        };
}
