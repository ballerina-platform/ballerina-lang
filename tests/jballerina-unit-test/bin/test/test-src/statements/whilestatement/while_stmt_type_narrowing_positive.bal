// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testWhileStmtTypeNarrow() returns true {
    test1("s");
    test2("s");
    test3();

    return true;
}

function test1(int|string? x) {
    while x != () {
        if x is int {
            int _ = x;
        } else {
            string _ = x; // OK
        }
        break;
    }

    while x != () {
        if x is int {
            int _ = x;
            return;
        }

        string _ = x; // OK
        break;
    }

    if x is int? {
        while x != () {
            int _ = x; // OK
            break;
        }
    }

    if x !is int? {
        string _ = x;

    } else {
        int? _ = x;

        while x != () {
            int _ = x; // OK
            break;
        }
    }
}

function test2(string|2|true? x) {
    while x !is () {
        while x != true {
            while !(x == 2) {
                string _ = x; // OK
                break;
            }
            break;
        }
        break;
    }
}

function test3() {
    int|boolean|string x = "x";
    while x !is int {
        do {
            if x is boolean {
                return;
            }

            string _ = x; // OK
        }
        break;
    }

    int|boolean|string y = "y";
    while y !is int {
        {
            if y is boolean {
                return;
            }

            string _ = y; // OK
        }
        break;
    }
}
