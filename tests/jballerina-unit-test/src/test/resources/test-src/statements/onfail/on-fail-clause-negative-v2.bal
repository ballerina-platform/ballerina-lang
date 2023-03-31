// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testQuery() returns error? {
    int i = 0;
    while (i > 2) {
        fail getError();
        i = i + 1;
    }
    i = 7;
    return;
}

function testReturnsWithOnFail1() returns int {
    do {
        return 1;
    } on fail {
    }
}

function testReturnsWithOnFail2() returns int {
    do {
        return 1;
    } on fail {
        return -1;
    }
}

function testReturnsWithOnFail3() returns int {
    do {
        int _ = 1;
    } on fail {
        return -1;
    }
}

function testReturnsWithOnFail4() returns int {
    do {
        int _ = 1;
    } on fail {
        int _ = -1;
    }
    return 0;
}

function testReturnsWithOnFail5() returns int|error {
    transaction {
        check commit;
        int _ = 1;
    } on fail {
        return -1;
    }
}

function testReturnsWithOnFail6() returns int|error {
    transaction {
        check commit;
        return 1;
    } on fail {
        return -1;
    }
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}
