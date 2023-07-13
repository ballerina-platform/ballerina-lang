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

function testUnInitVars9() {
    string str1;
    string str2;
    do {
    } on fail {
        str1 = "-> error caught. Hence value returning";
        str2 = "-> error caught. Hence value returning";
    }
    str1 += "-> reached end"; //error: variable 'str1' is not initialized
    str2 += "-> reached end"; //error: variable 'str2' is not initialized
}

function testUnInitVars10() {
    string str1;
    string str2;
    do {
        check getErrorOrNil();
        str2 = "partial init";
    } on fail {
        str1 = "partial init";
        str2 = "-> error caught. Hence value returning";
    }
    str1 += "-> reached end"; //error: variable 'str1' may not have been initialized
    str2 += "-> reached end";
}

function testUnInitVars11() {
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

function testUnInitVars12() {
    int i;
    int j;
    int k;
    do {
        i = 0;
        j = check getErrorOrInt();
        k = 1;
        j += 1;
    } on fail {
        k = -1;
    }
    i += 1;
    j += 1; //error: variable 'j' may not have been initialized
    k += 1;
}

function testUnInitVars13() {
    boolean bool = true;
    int i;
    int j;
    do {
        if (bool) {
            i = 1;
            check getErrorOrNil();
            j = 1;
        } else {
            i = 2;
            j = 2;
        }
        i += 1;
        j += 1;
    } on fail {
        j += 1; //error: variable 'j' may not have been initialized
    }
    i += 1;
    j += 1; //error: variable 'j' may not have been initialized
}

function testUnInitVars14() {
    int[] x;
    int[] y;
    int[] z;
    do {
        z = [];
        x = check getErrorOrIntArr();
        y = [];
        _ = x[0];
        _ = y[0];
        _ = z[0];
    } on fail {
        return;
    }
    _ = x[0];
    _ = y[0];
    _ = z[0];
}

function testUnInitVars15(boolean bool) {
    int[] x;
    int[] y;
    int[] z;
    do {
        if bool {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } else {
            z = [];
            x = [];
            y = [];
        }
        _ = x[0];
        _ = y[0];
        _ = z[0];
    } on fail {
        return;
    }
    _ = x[0];
    _ = y[0];
    _ = z[0];
}

function testUnInitVars16(boolean bool) {
    int[] i;
    do {
        do {
            i = check getErrorOrIntArr();
        }
    } on fail {
        if bool {
            i = [];
        } else {
            i = [];
        }
    }
    _ = i[0];
}

function testUnInitVars17(boolean bool) {
    int[] x;
    int[] y;
    int[] z;
    do {
        if bool {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } else {
            z = [];
            x = [];
            y = [];
        }
        _ = x[0];
        _ = y[0];
        _ = z[0];
    } on fail {
        _ = x[0]; //error: variable 'x' may not have been initialized
        _ = y[0]; //error: variable 'y' may not have been initialized
        _ = z[0];
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0]; //error: variable 'y' may not have been initialized
    _ = z[0];
}

function testUnInitVars18(boolean bool) {
    int[] x;
    int[] y;
    int[] z;
    do {
        if bool {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } else {
            z = [];
            x = [];
            y = [];
        }
        _ = x[0];
        _ = y[0];
        _ = z[0];
    } on fail {
        x = [];
    }
    _ = x[0];
    _ = y[0]; //error: variable 'y' may not have been initialized
    _ = z[0];
}

function testUnInitVars19() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail error e {
            fail e;
        }
        _ = x[0]; //no compilation error
        _ = y[0];
        _ = z[0];
    } on fail {
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0]; //error: variable 'y' may not have been initialized
    _ = z[0];
}

function testUnInitVars20() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail error e {
            fail e;
        }
        _ = x[0]; //no compilation error
        _ = y[0];
        _ = z[0];
    } on fail {
        x = [];
    }
    _ = x[0];
    _ = y[0]; //error: variable 'y' may not have been initialized
    _ = z[0];
}

function testUnInitVars21() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail error e {
            y = [];
            fail e;
        }
        _ = x[0]; //no compilation error
        _ = y[0];
        _ = z[0];
    } on fail {
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0];
    _ = z[0];
}

function testUnInitVars22() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail error e {
            y = [];
            fail e;
        }
        _ = x[0];
        _ = y[0];
        _ = z[0];
    } on fail {
        return;
    }
    _ = x[0];
    _ = y[0];
    _ = z[0];
}

function testUnInitVars23() {
    int[] x;
    int[] y;
    int[] z;

    do {
        z = [];
        x = [];
    } on fail {
        y = [];
    }
    _ = x[0];
    _ = y[0]; //error: variable 'y' is not initialized"
    _ = z[0];
}

function testUnInitVars24() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail {
            y = [];
        }
        _ = x[0]; //error: variable 'x' may not have been initialized
        _ = y[0];
        _ = z[0];
    } on fail {
        x = [];
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0];
    _ = z[0];
}

function testUnInitVars25() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail {
            y = [];
        }
        _ = x[0]; //error: variable 'x' may not have been initialized
        _ = y[0];
        _ = z[0];
    } on fail {
        _ = x[0]; //error: variable 'x' may not have been initialized
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0];
    _ = z[0];
}

function testUnInitVars26() {
    int[] x;
    int[] y;
    int[] z;
    do {
        do {
            z = [];
            x = check getErrorOrIntArr();
            y = [];
        } on fail {
            y = [];
        }
        _ = x[0]; //error: variable 'x' may not have been initialized
        _ = y[0];
        _ = z[0];
    } on fail {
        x = [];
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0];
    _ = z[0];
}

function testUnInitVars27(boolean bool) {
    int i;
    do {
        if (bool) {
            fail error("Dummy 1");
        } else {
            i = 0;
            fail error("Dummy 2");
        }
    } on fail {
        i = 0;
    }
    _ = i;
}

function testUnInitVars28(boolean bool) {
    int i;
    do {
        if (bool) {
            fail error("Dummy 1");
        } else {
            i = 0;
            fail error("Dummy 2");
        }
    } on fail {
    }
    _ = i; //error: variable 'i' may not have been initialized
}

function testUnInitVars29(boolean bool) returns error? {
    int[] x;
    int[] y;
    int[] z;

    do {
        do {
            z = [];
            if bool {
                x = check getErrorOrIntArr();
            } else {
                y = [];
            }
            _ = x[0]; //error: variable 'x' may not have been initialized
            _ = y[0]; //error: variable 'y' may not have been initialized
            _ = z[0];
        } on fail error e {
            fail e;
        }
        _ = x[0]; //error: variable 'x' may not have been initialized
        _ = y[0]; //error: variable 'y' may not have been initialized
        _ = z[0];
    } on fail error e {
        return e;
    }
    _ = x[0]; //error: variable 'x' may not have been initialized
    _ = y[0]; //error: variable 'y' may not have been initialized
    _ = z[0];
}

function testUnInitVars30(boolean bool) returns error? {
    int i;
    do {
        if bool {
            fail error("Dummy error");
        }
        _ = i; //error: variable 'x' is not initialized
    } on fail {
        i = 0;
    }
    _ = i; //error: variable 'x' may not have been initialized
}

function testUnInitVars31(boolean bool) {
   int i;
   do {
	   i = 0;
       if bool {
           fail error("Dummy 1");
       } else {
           fail error("Dummy 2");
       }
       _ = i; //error: unreachable code
   } on fail {
       i = 0;
   }
   // the following line should not result in a compile-time error
   _ = i;
}

function testUnInitVars32(boolean bool) {
    int i;
    do {
        i = 0;
        if bool {
            fail error("Dummy 1");
        } else {
            fail error("Dummy 2");
        }
    } on fail {
    }
    // the following line should not result in a compile-time error
    _ = i;
}

function testUnInitVars33(boolean bool) {
    int a = 1;
    final int c;
    do {
        if (a == 3) {
            fail error("Dummy error");
        }
    } on fail {
        c = 0;
    }
    // the following line should result in a compile-time error
    // variable c may not have been initialized
    _ = c;
}

function testUnInitVars34() {
    int i;
    do {
        if true {
            fail error("Dummy error");
        }
    } on fail {
        i = 0;
    }
    // the following line should not result in a compile-time error
    _ = i;
}

function testUnInitVars35(boolean bool) {
    int i;
    do {
        if bool {
            fail error("Dummy error");
        }
    } on fail {
        i = 0;
    }
    // the following line should result in a compile-time error
    // variable i may not have been initialized
    _ = i;
}

function testUnInitVars36() {
    int i;
    int j;
    do {
        i = check getErrorOrInt();
        return;
    } on fail {
        i = 0;
        j = 0;
    }
    // the following line should not result in a compile-time error
    _ = i;
    _ = j;
}

function testUnInitVars37() {
    int[] i;
    do {
    } on fail {
        lock {
            _ = i[0]; //error: variable 'i' is not initialized
        }
    }
}

function testUnInitVars38() {
    int[] i;
    do {
        i = check getErrorOrIntArr();
    } on fail {
        lock {
            _ = i[0]; //error: variable 'i' may not have been initialized
        }
    }
}

function testUnInitVars39() {
    int[] i;
    do {
        i = [];
        i = check getErrorOrIntArr();
    } on fail {
        lock {
            _ = i[0]; //no compilation error
        }
    }
}

function testUnInitVars40() returns error? {
    int[] i;
    transaction {
        check commit;
    } on fail {
        lock {
            _ = i[0]; //error: variable 'i' is not initialized
        }
    }
}

function testUnInitVars41() returns error? {
    int[] i;
    transaction {
        check commit;
    } on fail {
        i = [];
    }
    _ = i[0]; //error: variable 'i' may not have been initialized
}

function testUnInitVars42(boolean bool) {
    int[] i;
    foreach int j in 1 ... 3 {
        if bool {
            i = [check getErrorOrInt()];
        }
        break;
    } on fail {
        i = [];
    }
    _ = i[0]; //error: variable 'i' may not have been initialized
}

function testUnInitVars43(boolean bool) returns error? {
    int[] i;
    foreach int j in 1 ... 3 {
        if bool {
            i = [check getErrorOrInt()];
        } else {
            i = [check getErrorOrInt()];
        }
        break;
    } on fail {
        i = [];
    }
    _ = i[0]; //no compilation error
}

function testUnInitVars44(boolean bool) {
    int i;
    do {
        if bool {
            fail error("", message = "error");
        } else {
            i = 1;
        }
    } on fail {
    }
    _ = i; //error: variable 'i' may not have been initialized
}

function testUnInitVars45(int count) {
    int i = 0;
    int j;
    while count > i {
        check getErrorOrNil();
        j = i;
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars46(int count) {
    int i = 0;
    int j;
    while count > i {
        check getErrorOrNil();
        j = i;
    } on fail {
        j = 1;
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars47() {
    int i = 0;
    int j;
    while true {
        check getErrorOrNil();
        j = i;
    } on fail {
        j = 1;
    }
    _ = j; //no compile error
}

function testUnInitVars48() {
    int i = 0;
    int j;
    while true {
        check getErrorOrNil();
        j = i;
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars49() {
    int i = 0;
    int j;
    while true {
        check getErrorOrNil();
        j = i;
        return;
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars50() {
    int i = 0;
    int j;
    while true {
        check getErrorOrNil();
        j = i;
        return;
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars51() {
    int i = 0;
    int j;
    while true {
        check getErrorOrNil();
        j = i;
        return;
    } on fail {
        j = 1;
    }
    _ = j; //no compilation error
}

function testUnInitVars52(boolean bool) {
    int i = 0;
    int j;
    while bool {
        check getErrorOrNil();
        j = i;
        return;
    } on fail {
        j = 1;
    }
    _ = j; //no compilation error
}

function testUnInitVars53(boolean bool) {
    int i = 0;
    int j;
    do {
        while bool {
            check getErrorOrNil();
            j = i;
        } on fail {
            j = 1;
        }
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars54() {
    int i = 0;
    int j;
    do {
        while true {
            check getErrorOrNil();
            j = i;
        } on fail {
            j = 1;
        }
    } on fail {
    }
    _ = j; //no compilation error
}

function testUnInitVars55() {
    int i = 0;
    int j;
    do {
        while true {
            check getErrorOrNil();
            j = i;
        } on fail error e {
            fail e;
        }
    } on fail {
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars56() {
    int i = 0;
    int j;
    do {
        while true {
            check getErrorOrNil();
            j = i;
        } on fail error e {
            fail e;
        }
    } on fail {
        j = 1;
    }
    _ = j; //no compilation error
}

function testUnInitVars57(boolean bool) {
    int i = 0;
    int j;
    do {
        while bool {
            check getErrorOrNil();
            j = i;
        } on fail error e {
            fail e;
        }
    } on fail {
        j = 1;
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars58() {
    int i = 0;
    int j;
    do {
        foreach int _ in [1] {
            check getErrorOrNil();
            j = i;
        } on fail error e {
            fail e;
        }
    } on fail {
        j = 1;
    }
    _ = j; //no compilation error
}

function testUnInitVars59() {
    int i = 0;
    int j;
    do {
        foreach int _ in [1] {
            check getErrorOrNil();
            j = i;
        } on fail {
        }
    } on fail {
        j = 1;
    }
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars60() {
    int i;
    int j;
    do {
        check getErrorOrNil();
        [i, j] = [1, 2];
    } on fail {
    }
    _ = i; //error: variable 'i' may not have been initialized
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars61() {
    int i;
    int j;
    do {
        foreach int _ in [1] {
            check getErrorOrNil();
            [i, j] = [1, 2];
        } on fail {
        }
    } on fail {
        [i, j] = [1, 2];
    }
    _ = i; //error: variable 'i' may not have been initialized
    _ = j; //error: variable 'j' may not have been initialized
}

function testUnInitVars62() {
    int i;
    int j;
    do {
        foreach int _ in [1] {
            check getErrorOrNil();
            [i, j] = [1, 2];
        } on fail error e {
            fail e;
        }
    } on fail {
        [i, j] = [1, 2];
    }
    _ = i; //no compilation error
    _ = j; //no compilation error
}

public function testUnInitVars63() returns error? {
    int i;
    do {
        error? e = getError();
        if e is error {
            fail e;
        }
        i = 1;
    } on fail {
    }
    _ = i; //error: variable 'i' may not have been initialized
}

public function testUnInitVars64() returns error? {
    int i;
    do {
        error? e = getError();
        if e is error {
            fail e;
        }
        i = 1;
    } on fail {
        i = -1;
    }
    _ = i; //error: variable 'i' may not have been initialized
}

function getErrorOrIntArr() returns int[]|error {
    return getError();
}

public function testUnInitVars65() returns error? {
    int resultInt;
    do {
        resultInt = check getErrorOrInt();
    } on fail {
        resultInt = check getErrorOrInt();
    }
    resultInt += 1;
}

public function testUnInitVars66() returns error? {
    int resultInt;
    do {
        resultInt = check getErrorOrInt();
        do {
            resultInt = check getErrorOrInt();
        } on fail {
        }
    } on fail {
        resultInt = check getErrorOrInt();
    }
    resultInt += 1;
}

public function testUnInitVars67() returns error? {
    int resultInt1;
    int resultInt2;
    do {
        resultInt1 = check getErrorOrInt();
        do {
            resultInt2 = check getErrorOrInt();
        } on fail {
        }
    } on fail {
        resultInt1 = check getErrorOrInt();
    }
    resultInt1 += 1;
    resultInt2 += 1;
}
