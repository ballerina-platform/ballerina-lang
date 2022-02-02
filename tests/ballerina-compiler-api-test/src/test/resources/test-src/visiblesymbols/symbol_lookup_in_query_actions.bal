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

int [] arr1 = [1, 2, 3];
int [] arr2 = [1, 2, 3];

function test() {
    int result = 0;

    error? res1 = from int i in arr1
                   from int j in arr2
                   where i == j
                   do { result = i + j; };

    var res2 = trap from int i in arr1
              from int j in arr2
              let string stringVal = "abc", int intVal = -1
              do { result = i + j + intVal; };

    check from int i in arr1
                join int j in arr2
                on i equals j
                do { };

    checkpanic from int i in (from int k in arr1 select k)
                do { };
}
