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

int weightConst = 1000000;

string[] log = [];
int len = 0;

function orderingTest() returns string[] {
    future<()> ap = start asyncFunction();
    int i = 0;
    while(i < weightConst){
        i += 1;
    }

    wait ap;

    append(log, "parent: end");
    return log;
}


function asyncFunction() {
    append(log, "child: end");
}


function append(string[] arr, string msg) {
    arr[len] = msg;
    len = len + 1;
}
