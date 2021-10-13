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

import ballerina/lang.value;

int i = 1;

function nonIsolatedFn(any a) returns boolean {
    i += 1;
    return i % 2 == 0 && a is int;
}

isolated int j = 1;

isolated function isolatedFn(anydata a) returns boolean {
    lock {
        j += 1;
        return j % 2 == 0 && a is int[] && a[1] == 2;
    }
}

isolated function isolatedFn2(anydata... a) returns boolean {
    return a.length() == 0;
}

isolated function isolatedFn3(anydata[] a) returns boolean {
    return a.length() == 0;
}

function f1() {
    int k = i;

    match k {
        0 => {
        }
        1 if nonIsolatedFn(k) => {
        }
    }
}

function f2() {
    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if nonIsolatedFn(rec2) => {
        }
    }
}

function f3() {
    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if nonIsolatedFn(rec) => {
        }
    }
}

function f4() {
    int k = i;

    match k {
        1 if isolatedFn(k) => {
        }
    }
}

function f5() {
    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if isolatedFn(rec2) => {
        }
    }
}

function f6() {
    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if isolatedFn(rec) => {
        }
        var {x, y, z} => {
        }
        var {x} if isolatedFn(rec) => {
        }
    }
}

function f7() {
    int[] arr = [1, 2, 3];

    match arr {
        [1, 2] => {
        }
        [1, 2, 3] if isolatedFn(arr.cloneReadOnly()) => {
        }
    }
}

function f8() {
    int[] arr = [1, 2, 3];
    anydata[] & readonly anydataArr = [1, arr.cloneReadOnly(), value:cloneReadOnly(arr)];
    int m = 2;

    match arr {
        [1, 2] if isolatedFn2(1, arr.cloneReadOnly(), value:cloneReadOnly(arr)) => {
        }
        [] if isolatedFn2(...anydataArr) => {
        }
        [1, 2, 3] if isolatedFn(arr.cloneReadOnly()) => {
        }
        [1, 2, 3, 4] if isolatedFn3(a = anydataArr) => {
        }
        [1, 2, 3, 4] if arr.cloneReadOnly().indexOf(1) == 0 => {
        }
    }
}

isolated class IsolatedClass {
    private record {
        1|2|3 x;
    } r = {x: 2};

    isolated function isolatedFn(anydata x) returns boolean {
        lock {
            self.r.x = <1|2|3> x;
            return x == 1;
        }
    }

    function nonIsolatedFn(anydata... a) returns boolean {
        i += 1;
        return i / 2 > 1;
    }

    isolated function isolatedFn2(anydata... a) returns boolean {
        return a.length() == 0;
    }

    isolated function isolatedFn3(anydata[] a) returns boolean {
        return a.length() == 0;
    }
}

class NonIsolatedClass {
    int l = 1;

    isolated function isolatedFn(anydata m) returns boolean {
        lock {
            return (<int> m + j) % 3 == 1;
        }
    }

    function nonIsolatedFn(anydata x) returns boolean {
        self.l += 1;
        return self.l / 2 > 1;
    }

    isolated function isolatedFn2(anydata... a) returns boolean {
        return a.length() == 0;
    }

    isolated function isolatedFn3(anydata[] a) returns boolean {
        return a.length() == 0;
    }
}

function f9() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;
    int k = i;

    match k {
        0 => {
        }
        1 if c1.nonIsolatedFn(k) || c1.nonIsolatedFn(1) => {
        }
        2 if c2.nonIsolatedFn(k) => {
        }
        3 if k is 1|2 && c2.nonIsolatedFn(k) => {
        }
    }
}

function f10() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;

    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, z} if c1.nonIsolatedFn(rec2) || c2.nonIsolatedFn(rec2) => {
        }
    }
}

function f11() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;

    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if c1.nonIsolatedFn(rec) => {
        }
        var {x, z, a} if c2.nonIsolatedFn(rec) => {
        }
        var {} if c1.nonIsolatedFn(rec) => {
        }
    }
}

function f12() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;
    1|2 k = 1;

    match k {
        1 if c1.isolatedFn(k) => {
        }
        2 if c2.isolatedFn(k) => {
        }
    }
}

function f13() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;

    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if c1.isolatedFn(rec2) => {
        }
        var {x, z} if c2.isolatedFn(rec2) => {
        }
    }
}

function f14() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;

    record { int x; string y?; } rec = {x: 1, y: ""};
    map<anydata> & readonly rec2 = rec.cloneReadOnly();

    match rec2 {
        var {x, y} if c1.isolatedFn(rec) => {
        }
        var {x, y, z} => {
        }
        var {x} if c2.isolatedFn(rec) => {
        }
    }
}

function f15() {
    IsolatedClass c1 = new;
    int[] arr = [1, 2, 3];

    match arr {
        [1, 2] => {
        }
        [1, 2, 3] if c1.isolatedFn(arr.cloneReadOnly()) => {
        }
    }
}

function f16() {
    IsolatedClass c1 = new;

    int[] arr = [1, 2, 3];
    anydata[] & readonly anydataArr = [1, arr.cloneReadOnly(), value:cloneReadOnly(arr)];
    int m = 2;

    match arr {
        [1, 2] if c1.isolatedFn2(1, arr.cloneReadOnly(), value:cloneReadOnly(arr)) => {
        }
        [] if c1.isolatedFn2(...anydataArr) => {
        }
        [1, 2, 3] if c1.isolatedFn(arr.cloneReadOnly()) => {
        }
        [1, 2, 3, 4] if c1.isolatedFn3(a = anydataArr) => {
        }
    }
}

// To be uncommented after https://github.com/ballerina-platform/ballerina-lang/issues/33216 is fixed.
// function f17() {
//     int[] x = [1, 2];
//
//     match x {
//         [1, 2] if function () { _ = nonIsolatedFn(1); } is isolated function () => {
//         }
//     }
// }

function f18() {
    int[] x = [1, 2];

    match x {
        // To be uncommented after https://github.com/ballerina-platform/ballerina-lang/issues/33216 is fixed.
        // [1, 2] if function () {
        //                 int y = nonIsolatedFn("") ? 1 : 2;
        //                 NonIsolatedClass c = new;
        //                 match y {
        //                     1 if nonIsolatedFn("") || c.nonIsolatedFn(2) => {
        //                     }
        //                 }
        //             } is isolated function () => {
        // }
        [1, 2] if service object {
                        boolean b = isolatedFn([]);

                        function f1() {
                            int y = 2;
                            IsolatedClass c = new;
                            match y {
                                1 if nonIsolatedFn("") || c.nonIsolatedFn(2) => {
                                }
                            }
                        }

                        remote function f2() {
                            IsolatedClass c = new;
                            int y = c.nonIsolatedFn(2) ? 0 : 1;
                            match y {
                                1 if nonIsolatedFn("") => {
                                }
                            }
                        }

                        resource function get f3() {
                            NonIsolatedClass c = new;
                            int y = c.nonIsolatedFn(2) ? 0 : 1;
                            match y {
                                1 if nonIsolatedFn("") => {
                                }
                            }
                        }
                    } is service object {function f1();} => {
        }
    }
}
