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

type MyError1 error<record { int x; }>;
type MyError2 error<record { int y; }>;
type MyError3 error;
type MyError4 error;
type MyError5 distinct error;
type MyError6 distinct error;
type MyError7 distinct error & readonly;
type MyError8 distinct error & readonly;
type MyError9 distinct error|int;
type MyError10 distinct error|int;

function testNegative1() {
    int v1 = 0;
    match v1 {
        error() => {} // pattern will not be matched
    }

    any v2 = 0;
    match v2 {
        error() => {} // pattern will not be matched
    }

    MyError1 v3 = error MyError1("Message", x = 2);
    match v3 {
        error MyError2(y = 4) => {} // pattern will not be matched
    }
}

function testNegative2() {
    any|error v = 2;
    match v {
        error() | error() => {} // unreachable pattern
        error("Message") | error("Message") => {} // unreachable pattern
        error(_) | error(_) => {} // unreachable pattern
        error(var a) | error(var b) => {} // unreachable pattern
        error("Message", error("foo")) | error("Message", error("foo")) => {} // unreachable pattern
        error("Message", error("Msg"), x = 2) | error("Message", error("Msg"), x = 2) => {} // unreachable pattern
        error("Message", error("Msg"), x = 4) | error("Message", error("Msg"), x = 3) => {}
        error MyError1("Message", x = 4) | error MyError1("Message", x = 4) => {} // unreachable pattern
    }
}

function testNegative3() {
    error v1 = getCustomError();
    match v1 {
        var errorA if v1 is MyError1 => { } // unused variable 'errorA'
        var errorB if v1 is MyError2 => { } // unused variable 'errorB'
    }

    error? v2 = getCustomError();
    match v2 {
        var errorC if v2 is MyError1 => { } // unused variable 'errorC'
        var errorD if v2 is MyError2 => { } // unused variable 'errorD'
    }
}

function testNegative4() {
    error v1 = getCustomError();
    match v1 {
        var errorA if v1 is MyError3 => { } // unused variable 'errorA'
                                            // unnecessary condition: expression will always evaluate to 'true'
        var errorB if v1 is MyError4 => { } // unused variable 'errorB', unreachable pattern
                                            // unnecessary condition: expression will always evaluate to 'true'
    }

    error? v2 = getCustomError();
    match v2 {
        var errorC if v2 is MyError3 => { } // unused variable 'errorC'
        var errorD if v2 is MyError4 => { } // unused variable 'errorD', unreachable pattern
    }
}

function testNegative5() {
    error v1 = getCustomError();
    match v1 {
        var errorA if v1 is MyError5 => { } // unused variable 'errorA'
        var errorB if v1 is MyError6 => { } // unused variable 'errorB'
    }

    error? v2 = getCustomError();
    match v2 {
        var errorC if v2 is MyError5 => { } // unused variable 'errorC'
        var errorD if v2 is MyError6 => { } // unused variable 'errorD'
    }
}

function testNegative6() {
    error v1 = getCustomError();
    match v1 {
        var errorA if v1 is MyError7 => { } // unused variable 'errorA'
        var errorB if v1 is MyError8 => { } // unused variable 'errorB'
    }

    error? v2 = getCustomError();
    match v2 {
        var errorC if v2 is MyError7 => { } // unused variable 'errorC'
        var errorD if v2 is MyError8 => { } // unused variable 'errorD'
    }
}

function testNegative7() {
    error v1 = getCustomError();
    match v1 {
        var errorA if v1 is MyError9 => { } // unused variable 'errorA'
        var errorB if v1 is MyError10 => { } // unused variable 'errorB'
    }

    error? v2 = getCustomError();
    match v2 {
        var errorC if v2 is MyError9 => { } // unused variable 'errorC'
        var errorD if v2 is MyError10 => { } // unused variable 'errorD'
    }
}

function getCustomError() returns error {
    return error MyError1("Message", x = 2);
}
