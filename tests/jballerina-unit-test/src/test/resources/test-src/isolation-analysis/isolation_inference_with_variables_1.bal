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

import ballerina/lang.array;

int[] a = getIntArray();

int[][] b = [a.cloneReadOnly(), getIntArray()];

record {
    int[] i;
    boolean[] j;
} c = {
    i: x,
    j: getBooleanArray(<int[3]> getIntArray())
};

object {
    int[] i;
    string[] j;
} d = object {
    int[] i = getIntArray();
    string[] j;

    function init() {
        self.j = [];
    }
};

NonIsolatedObjectIIV1 e = new NonIsolatedClassWithIsolatedInitIIV1(getIntArray(), ["hello", "world"]);

final readonly & int[] x = [1, 2, 3];

function getIntArray() returns int[] => x;

type NonIsolatedObjectIIV1 object {
    int[] i;
    string[] j;
};

class NonIsolatedClassWithIsolatedInitIIV1 {
    int[] i;
    string[] j;

    isolated function init(int[] i, string[] j) {
        self.i = i;
        self.j = j;
    }
}

isolated function getBooleanArray(int[] arr) returns boolean[] {
    return <boolean[]> arr.'map(intVal => intVal < 0);
}

function testIsolatedVariableAccessInLock(boolean bool) {
    int[][] arr;
    lock {
        arr = b.clone();
    }

    if bool {
        int index = 1;
        int j = arr[0][0];

        lock {
            while j != 0 {
                var cClone = c.clone();
                cClone.i = [index];
                j = arr[index][index];
                index += 1;
            }
        }
    }
}

function testAccessingIsolatedVariableInIsolatedFunction() returns function (int) {
    lock {
        int[][] arr = b.cloneReadOnly();
    }

    var fn = function (int i) {
        lock {
            int[][] clone = b.clone();
            clone.push([i]);
        }
    };
    return fn;
}

int[] stack = [];

int[][] allStacks = [];

function testValidTransferOutInLockAccessingIsolatedVar() {
    lock {
        int[][] stacks = [stack];
        allStacks = stacks.clone();
    }
}

function addToAllStacks() {
    lock {
        stack.push(0);
    }
}

int[][] isolatedStacks = [];

function testValidTransferInUpdatingIsolatedVar(int[] n) {
    lock {
        isolatedStacks.push(n.cloneReadOnly());
    }
}

function testValidTransferOutUpdatingOfIsolatedVarClone() returns int[][] {
    lock {
        return isolatedStacks.clone();
    }
}

function testValidTransferOutUpdatingOfIsolatedVarMember(int index, boolean b) returns int[] {
    lock {
        if b {
            return isolatedStacks.cloneReadOnly()[index + 1];
        }
        return isolatedStacks[index].cloneReadOnly();
    }
}

function testValidTransferInAsArgInLockAccessingIsolatedVar(int[] n) {
    lock {
        update(isolatedStacks, n.clone());
    }
}

function update(int[][] x, int[] y) {
    x.push(y);
}

map<int> isolatedModuleLevelMap = {};

function copyInInMethodCall1() returns map<int>[] {
    map<int>[] y = [];

    lock {
        map<int>[] y2 = y.clone();
        y2[0] = isolatedModuleLevelMap;
        y.clone().push(isolatedModuleLevelMap);
        array:push(y.clone(), isolatedModuleLevelMap);
        array:push(y2, isolatedModuleLevelMap);
    }

    return y;
}

function copyInInMethodCall2(map<int[]> y) {
    lock {
        _ = y.clone().remove(isolatedModuleLevelMap["a"].toString());
    }
}

function copyOutAccessingIsolatedVar() returns map<int>[] {
     map<int>[] y = [];
     map<int> z;
     lock {
         map<int>[] y2 = [];
         y2[0] = isolatedModuleLevelMap;
         z = isolatedModuleLevelMap.cloneReadOnly();
         return y2.clone();
     }
}

class NonIsolatedClassIIV1 {
    int i = 2;
}

IsolatedClassIIV1 varInferredAsIsolated1 = new;
NonIsolatedClassIIV1 varInferredAsIsolated2 = new;

final IsolatedClassIIV1 varWithTypeInferredAsIsolated1 = new;
final IsolatedClassIIV1|int varWithTypeInferredAsIsolated2 = new;

function f1() returns IsolatedClassIIV1 {
    varWithTypeInferredAsIsolated1.func();
    lock {
        return varInferredAsIsolated1;
    }
}

function f2() {
    lock {
        _ = varInferredAsIsolated2;
    }

    var val = varWithTypeInferredAsIsolated2;
    int x = val is int ? val : 0;

    lock {
        var val2 = varWithTypeInferredAsIsolated2;
        varInferredAsIsolated2.i += val2 is int ? val2 : 0;
        varInferredAsIsolated2.i += x;
    }
}

class IsolatedClassIIV1 {
    private int y = 1;

    function init() {
    }

    function func() {
    }
}

class OtherIsolatedClassIIV1 {
    final IsolatedClassIIV1 x = f1();
    private int y = 1;

    function init() {
    }

    function func() {
        lock {
            lock {
                _ = f1();
                self.y += 1;
                f2();
            }
        }
    }
}

final NonIsolatedClassIIV1 varInferredAsIsolatedWithTypeNotInferredAsIsolated = new;
final NonIsolatedClassIIV1 varNotInferredAsIsolateWithTypeNotInferredAsIsolated = new;

function f3() {
    lock {
        varInferredAsIsolatedWithTypeNotInferredAsIsolated.i = 3;
    }
}

function f4() {
    varNotInferredAsIsolateWithTypeNotInferredAsIsolated.i = 3;
}

public function testIsolatedInference() {
    assertTrue(getIntArray is isolated function() returns int[]);
    assertTrue(testAccessingIsolatedVariableInIsolatedFunction is isolated function() returns function (int));
    assertTrue(testAccessingIsolatedVariableInIsolatedFunction() is isolated function(int));
    assertFalse(testValidTransferOutInLockAccessingIsolatedVar is isolated function());
    assertFalse(<any> addToAllStacks is isolated function);
    assertTrue(<any> testValidTransferInUpdatingIsolatedVar is isolated function);
    assertTrue(<any> testValidTransferOutUpdatingOfIsolatedVarClone is isolated function);
    assertTrue(<any> testValidTransferOutUpdatingOfIsolatedVarMember is isolated function);
    assertTrue(<any> update is isolated function);
    assertTrue(<any> testValidTransferInAsArgInLockAccessingIsolatedVar is isolated function);
    assertTrue(<any> copyInInMethodCall1 is isolated function);
    assertTrue(<any> copyInInMethodCall2 is isolated function);
    assertTrue(<any> copyOutAccessingIsolatedVar is isolated function);
    assertTrue(<any> f1 is isolated function);
    assertTrue(<any> f2 is isolated function);
    assertTrue(<any> f3 is isolated function);
    assertFalse(<any> f4 is isolated function);
    assertTrue(<any> new IsolatedClassIIV1() is isolated object {});
    assertTrue(<any> new OtherIsolatedClassIIV1() is isolated object {});
    assertFalse(<any> new NonIsolatedClassIIV1() is isolated object {});
}

isolated function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

isolated function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
