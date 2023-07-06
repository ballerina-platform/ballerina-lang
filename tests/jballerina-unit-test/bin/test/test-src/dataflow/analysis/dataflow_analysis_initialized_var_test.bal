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

function testInitializedVarWithWhile1() {
    int i;

    while true {
        i = 1;
        break;
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile2() {
    int i;
    boolean b = true;

    while true {
        if b {
            i = 1;
        } else {
            i = 2;
        }
        break;
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile3() {
    int i;
    boolean b = false;

    while true {
        while b {
            i = 1;
            break;
        }
        i = 2;
        break;
    }

    int j = i;
    assertEquality(2, j);
}

function testInitializedVarWithWhile4() {
    int i;
    boolean b = false;

    while true {
        while true {
            if b {
                i = 2;
            } else {
                i = 3;
            }
            break;
        }
        break;
    }

    int j = i;
    assertEquality(3, j);
}

function testInitializedVarWithWhile5() {
    int i;

    if true {
        while true {
            i = 1;
            break;
        }
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile6() {
    int i;

    if true {
        while true {
            boolean c = true;
            if c {
                i = 1;
            } else {
                i = 2;
            }
            break;
        }
    }

    int j = i;
    assertEquality(1, j);
}

function testInitializedVarWithWhile7() {
    int i;
    boolean b = true;

    if b {
        while true {
            boolean c = true;
            while true {
                if c {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            }
            break;
        }
    } else {
        i = 3;
    }

    int j = i;
    assertEquality(1, j);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
