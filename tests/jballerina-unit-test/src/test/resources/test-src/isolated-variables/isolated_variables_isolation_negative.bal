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

    var fn = isolated function() {
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
