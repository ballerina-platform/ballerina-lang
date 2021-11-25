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

isolated int[][] b = [a.cloneReadOnly(), getIntArray()];

isolated record {
    int[] i;
    boolean[] j;
} c = {
    i: x,
    j: getBooleanArray(<int[2]> getIntArray())
};

isolated object {
    int[] i;
    string[] j;
} d = object {
    int[] i = getIntArray();
    string[] j;

    isolated function init() {
        self.j = [];
    }
};

isolated NonIsolatedObject e = new NonIsolatedClassWithIsolatedInit(getIntArray(), ["hello", "world"]);

final readonly & int[] x = [1, 2, 3];

isolated function getIntArray() returns int[] => x;

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

isolated function testAccessingIsolatedVariableInIsolatedFunction() {
    lock {
        int[][] arr = b.cloneReadOnly();
    }

    var fn = isolated function (int i) {
        lock {
            int[][] clone = b.clone();
            clone.push([i]);
        }
    };
}

isolated int[] stack = [];

int[][] allStacks = [];

function testValidTransferOutInLockAccessingIsolatedVar() {
    lock {
        int[][] stacks = [stack];
        allStacks = stacks.clone();
    }
}

isolated int[][] isolatedStacks = [];

isolated function testValidTransferInUpdatingIsolatedVar(int[] n) {
    lock {
        isolatedStacks.push(n.cloneReadOnly());
    }
}

isolated function testValidTransferOutUpdatingOfIsolatedVarClone() returns int[][] {
    lock {
        return isolatedStacks.clone();
    }
}

isolated function testValidTransferOutUpdatingOfIsolatedVarMember(int index, boolean b) returns int[] {
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

isolated function update(int[][] x, int[] y) {
    x.push(y);
}

isolated map<int> isolatedModuleLevelMap = {};

isolated function copyInInMethodCall1() returns map<int>[] {
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

isolated int[] i = [1, 2];

isolated function testIsolationAnalysisWithOnFailStatement() {
    do {

    } on fail error e {
        lock {
            int k = i.pop();
        }
    }

    anydata l = 1;
    lock {
        anydata val = l.clone();
        match val {
            "1" => {
                lock {
                    int k = check int:fromString(val.toString()) + i[0];
                }
            }
        } on fail var e {
            i.push(e.message().length());
        }
    }

    foreach var item in 1 ..< 2 {

    } on fail error e {
        lock {
            i.push(1);
        }
    }

    int m = 1;

    while m < 2 {
        m += 1;
    } on fail error e {
        lock {
            i.push(m);
        }
    }

    lock {
        lock {

        } on fail error e {
            i[i.length()] = 1;
        }
    }
}

isolated int[] intArray = [];

function testRangeExprBeingAnIsolatedExpression1() returns object {} {
    lock {
        return intArray[0] ... intArray[1] + 1;
    }
}

function testRangeExprBeingAnIsolatedExpression2() returns object {} {
    lock {
        int m = intArray[0];
        return m + 1 ..< m * 2;
    }
}
