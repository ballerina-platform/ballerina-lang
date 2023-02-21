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

function getErrorOrInt() returns int|error {
    return getError();
}

public function testUnInitVars1() {
    int resultInt;
    do {
        resultInt = check getErrorOrInt();
    } on fail {
    }
    resultInt += 1;
}

public function testUnInitVars2() {
    int resultInt1;
    int resultInt2;
    int resultInt3;
    do {
        resultInt1 = 1;
        resultInt2 = check getErrorOrInt();
        resultInt3 = 1;
    } on fail {
    }
    resultInt1 += 1;
    resultInt2 += 1;
    resultInt3 += 1;
}

public function testUnInitVars3() {
    int resultInt1;
    int resultInt2;
    int resultInt3;
    transaction {
        check commit;
        resultInt1 = 1;
        resultInt2 = check getErrorOrInt();
        resultInt3 = 1;
    } on fail {
    }
    resultInt1 += 1;
    resultInt2 += 1;
    resultInt3 += 1;
}

public function testUnInitVars4() {
    int resultInt1;
    int resultInt2;
    int resultInt3;
    transaction {
        do {
           resultInt1 = 1;
           resultInt2 = check getErrorOrInt();
           resultInt3 = 1;
        }
        check commit;
    } on fail {
    }
    resultInt1 += 1;
    resultInt2 += 1;
    resultInt3 += 1;
}

public function testUnInitVars5() {
    int resultInt1;
    int resultInt2;
    int resultInt3;
    transaction {
        do {
           resultInt1 = 1;
           resultInt2 = 2;
        }
        check commit;
        resultInt3 = 1;
    } on fail {
    }
    resultInt1 += 1;
    resultInt2 += 1;
    resultInt3 += 1;
}

function testUnInitVars6() {
    int i;
    int j;
    int k;
    do {
        i = 0;
        j = 1;
        check getErrorOrNil();
        k = 1;
    } on fail {
        i += 1;
        j += 1;
        k += 1;
    }
}

function testUnInitVars7() {
    int i;
    int j;
    int k;
    do {
        i = 0;
        j = 1;
        check getErrorOrNil();
        k = 1;
    } on fail {
        k = -1;
    }
    i += 1;
    j += 1;
    k += 1;
}

function getErrorOrNil() returns error? {
    return getError();
}

function testUnInitVars8(int[] data) returns string {
    string str1 = "";
    string str2;
    foreach var i in data {
        if(i < 0) {
            check getErrorOrNil();
            str1 = "partial init";
            str2 = "partial init";
        }
    } on fail {
        str1 += "-> error caught. Hence value returning";
        return str1;
    }
    str2 += "-> reached end";
    return str1;
}

