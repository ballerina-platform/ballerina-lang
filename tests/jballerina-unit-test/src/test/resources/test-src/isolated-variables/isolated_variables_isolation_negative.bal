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
import ballerina/lang.array;

int[] a = getIntArray();
isolated int[][] b = [a, getIntArray()];

isolated record {
    int[] i;
    boolean[2] j;
} c = {
    i: a,
    j: getBooleanArray(<int[2]> getIntArray())
};

isolated object {
    int[] i;
    string[] j;
} d = object {
    int[] i = getIntArray();
    string[] j;

    function init() {
        self.j = [];
    }
};

isolated NonIsolatedObject e = new NonIsolatedClassWithIsolatedInit(getIntArray(), ["hello", "world"]);

int[] x = [1, 2, 3];

function getIntArray() returns int[] => x;

type NonIsolatedObject object {
    int[] i;
    string[] j;
};

class NonIsolatedClassWithIsolatedInit {
    int[] i;
    string[] j;

    isolated function init(int[] i, string[] j) {
        self.i = i;
        self.j = j;
    }
}

isolated function getBooleanArray(int[2] arr) returns boolean[2] {
    return <boolean[2]> arr.'map(intVal => intVal < 0);
}

function testInvalidIsolatedVariableAccessOutsideLock(boolean bool) {
    int[][] arr = b;

    if bool {
        int index = 1;
        int j = arr[0][0];

        while j != 0 {
            c.i = [index];
            j = arr[index][index];
            index += 1;
        }
    }

    var _ = isolated function() {
        int[][] arr2 = b;
    };
}

isolated class IsolatedObject {
    private map<int> m = {};

    isolated function testInvalidAccessOfMoreThanOneIsolatedVar1() {
        lock {
            int x = b[0][0];
            self.m["a"] = x;
        }
    }

    function testInvalidAccessOfMoreThanOneIsolatedVar2() {
        lock {
            int x = b[0][0];
            self.m["a"] = x;
            self.testInvalidAccessOfMoreThanOneIsolatedVar1();
        }
    }
}

isolated map<boolean>[] f = [];
map<boolean> globBoolMap = {a: true, b: false};

function testInvalidCopyInWithIsolatedVarAccessOne(map<boolean> boolMap) {
    map<boolean> bm1 = {};
    lock {
        map<boolean> bm2 = {a: true, b: false};
        f[0] = globBoolMap;
        f.push(boolMap);
        f = [bm1, bm2];
    }
}

isolated function testInvalidCopyInWithIsolatedVarAccessTwo(map<boolean> boolMap) {
    map<boolean> bm1 = {};
    lock {
        map<boolean> bm2 = {};
        lock {
            map<boolean> bm3 = boolMap;
            bm2 = bm3;
        }

        f.push(boolMap);
        f[0] = boolMap;
        f = [bm1, bm2];
        bm1 = f[0];
    }
}

function testInvalidCopyOutWithIsolatedVarOne(map<boolean>[] boolMaps) returns map<boolean>[] {
    map<boolean>[] bm1 = boolMaps;
    lock {
        map<boolean>[] bm2 = [{a: true, b: false}];
        bm2 = f.clone();
        f = bm2;
        return bm2;
    }
}

isolated function testIInvalidCopyOutWithIsolatedVarTwo(map<boolean>[] boolMaps) {
    map<boolean> bm1 = {};
    lock {
        map<boolean> bm2 = {};
        lock {
            map<boolean> bm3 = boolMaps[0].clone();
            bm1 = bm3;
            _ = nonIsolatedFunc();
        }
        f = boolMaps;
    }
}

function testInvokingNonIsolatedFunctionInLockAccessingIsolatedVar() {
    lock {
        f = [];
        _ = nonIsolatedFunc();
    }
}

isolated int g = 1;

isolated function testAccessingDifferentIsolatedVarsInNestedLockStatements() {
    lock {
        lock {
            int i = g;
        }

        int j = 2;
        f = [];
    }
}

function nonIsolatedFunc() returns int[] {
    return [1, 2, 3];
}

isolated int[] stack = [];

int[][] allStacks = [];

function testInvalidTransferOutInLockAccessingIsolatedVar() {
    lock {
        int[][] stacks = [stack]; // OK
        allStacks = stacks; // error
    }
}

isolated int[][] isolatedStacks = [];

function testInvalidTransferInUpdatingIsolatedVar(int[] n) {
    lock {
        isolatedStacks.push(n); // error for `n`, OK if it was `n.clone()`
    }
}

isolated function testInvalidTransferOutUpdatingOfIsolatedVar() returns int[][] {
    lock {
        return isolatedStacks; // error, OK if it was `isolatedStacks.clone()`
    }
}

isolated function testInvalidTransferOutUpdatingOfIsolatedVarMember(int index) returns int[] {
    lock {
        return isolatedStacks[index]; // error, OK if it was `isolatedStacks[index].clone()` or ` isolatedStacks.clone()[index]`
    }
}

function testInvalidTransferInAsArgInLockAccessingIsolatedVar(int[] n) {
    lock {
        update(isolatedStacks, n); // error, OK if `update(isolatedStacks, n.clone())`
    }
}

isolated function update(int[][] x, int[] y) {
    x.push(y);
}

isolated int[][] arr = [];

function getArrayMemberDirect() returns int[] {
    lock {
        return arr[0];
    }
}

function getArrayMemberViaFunctionCall() returns int[] {
    lock {
        return getMember(arr);
    }
}

isolated function getMember(int[][] x) returns int[] {
    return x[0];
}

isolated map<int[]> mp = {};

function getMapMemberDirect() returns int[] {
    lock {
        if true {
            return <int[]> mp["x"];
        } else {
            return mp.get("x");
        }
    }
}

function getMapMemberViaFunctionCall() returns int[] {
    lock {
        return getMapMember(mp);
    }
}

isolated function getMapMember(map<int[]> mp) returns int[] {
    return <int[]> mp["a"];
}

function getArrayMemberViaFunctionCall2() returns int[] {
    lock {
        int[][] arr2 = [arr[0]];
        return getMember(arr2);
    }
}

function getMapMemberViaFunctionCall2() returns int[] {
    lock {
        map<int[]> mp2 = {x: arr[0]};
        return getMapMember(mp2);
    }
}

class NonIsolatedClass {
    int[] m = [];

    isolated function getMember() {
        lock {
            arr[0] = self.m;
            arr.push(self.m);
        }
    }

    isolated function getMember2() returns int[] {
        lock {
            arr[0] = self.getMemberInternal();
            return getMemberUsingSelf(self);
        }
    }

    isolated function getMemberInternal() returns int[] {
        return self.m;
    }
}

isolated function getMemberUsingSelf(NonIsolatedClass cl) returns int[] => cl.getMemberInternal();

isolated map<int> isolatedModuleLevelMap = {};

isolated function invalidCopyInInMethodCall1() returns map<int>[] {
    map<int>[] y = [];

    lock {
        y[0] = isolatedModuleLevelMap;
        y.push(isolatedModuleLevelMap);
        array:push(y, isolatedModuleLevelMap);
        return y;
    }
}

function invalidCopyInInMethodCall2(map<int[]> y) {
    lock {
        _ = y.remove(isolatedModuleLevelMap["a"].toString());
    }
}

function invalidCopyOutAccessingIsolatedVar() returns map<int>[] {
    map<int>[] y = [];
    map<int> z;
    lock {
        map<int>[] y2 = [];
        y[0] = isolatedModuleLevelMap;
        z = isolatedModuleLevelMap;
        return y2;
    }
}

isolated isolated object {}[] isolatedIsolatedObjArr = [];

isolated function getArr() returns isolated object {}[] {
    lock {
        return from var ob in isolatedIsolatedObjArr select ob;
    }
}

isolated int[] i = [1, 2];

isolated function testIsolationAnalysisWithOnFailStatementNegative() {
    do {

    } on fail error e {
        int k = i.pop();
    }

    anydata l = 1;
    match l {
        "1" => {
            lock {
                int k = check int:fromString(l.toString()) + i[0];
            }
        }
    } on fail var e {
        i.push(e.message().length());
    }

    foreach var item in 1 ..< 2 {

    } on fail error e {
        i.push(1);
    }

    int m = 1;

    while m < 2 {
        m += 1;
    } on fail error e {
        i.push(m);
    }

    lock {

    } on fail error e {
        i[i.length()] = 1;
    }
}
