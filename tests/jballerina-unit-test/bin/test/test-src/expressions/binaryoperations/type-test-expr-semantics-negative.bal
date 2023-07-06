// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type A record {
    int x = 0;
    string y = "";
};

type B record {
    int x = 0;
};

function testSimpleRecordTypes() returns string {
    A a = {};
    if (a is B) {
        return "a is B";
    } else if (a is A) {
        return "a is A";
    }

    // checking against undefined type
    if (a is C) {
    
    }
    return "n/a";
}