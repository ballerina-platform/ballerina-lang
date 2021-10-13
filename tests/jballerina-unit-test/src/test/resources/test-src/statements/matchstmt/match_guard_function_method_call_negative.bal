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
    int[] arr = [1, 2, 3];

    match arr {
        [1, 2] => {
        }
        [1, 2, 3] if nonIsolatedFn(arr.cloneReadOnly()) => {
        }
        [1, 2, 4] if nonIsolatedFn(arr) => {
        }
    }
}

function f2() {
    int[] arr = [1, 2, 3];
    anydata[] anydataArr = [1, arr.cloneReadOnly(), value:cloneReadOnly(arr)];
    int m = 2;

    match arr {
        [1, 2] if isolatedFn2(1, arr, arr) => {
        }
        [] if isolatedFn2(...anydataArr) => {
        }
        [1, 2, 3] if isolatedFn(arr) => {
        }
        [1, 2, 3, 4] if isolatedFn3(a = anydataArr) => {
        }
        [1, 2, 3, 4] if arr.indexOf(1) == 0 => {
        }
        [1, 2, 3, 4] if arr.cloneReadOnly() == <readonly> [] => {
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

function f3() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;
    int[] arr = [1, 2, 3];

    match arr {
        [1, 2] => {
        }
        [1, 2, 3] if c1.isolatedFn(arr) => {
        }
        [1, 2, 4] if c2.isolatedFn(arr.cloneReadOnly()) => {
        }
        [2, 2] => {
        }
    }
}

function f4() {
    IsolatedClass c1 = new;
    NonIsolatedClass c2 = new;

    int[] arr = [1, 2, 3];
    anydata[] anydataArr = [1, arr.cloneReadOnly(), value:cloneReadOnly(arr)];
    int m = 2;

    match arr {
        [1, 2] if c1.isolatedFn2(1, arr, value:clone(arr)) => {
        }
        [] if c1.isolatedFn2(...anydataArr) => {
        }
        [1, 2, 3] if c1.isolatedFn(arr) => {
        }
        [1, 2, 3, 4] if c1.isolatedFn3(a = anydataArr) => {
        }
        [2, 3] if c2.isolatedFn(arr.cloneReadOnly()) => {
        }
        [2, 4] if c2.nonIsolatedFn(arr.cloneReadOnly()) || c1.nonIsolatedFn() => {
        }
    }
}
