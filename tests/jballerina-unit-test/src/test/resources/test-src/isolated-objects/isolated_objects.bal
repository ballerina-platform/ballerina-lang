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

public isolated class IsolatedClassWithNoMutableFields {
    public final int[] & readonly a;
    final readonly & record {int i;} b;

    function init(int[] & readonly a, record {int i;} & readonly b) {
        self.a = a;
        self.b = b;
    }
}

isolated class IsolatedClassWithPrivateMutableFields {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b;

    private int c;
    private map<int>[] d;

    isolated function init(record {int i;} & readonly b, int c) {
        self.b = b;
        self.c = c;
    }
}

final readonly & string[] immutableStringArray = ["hello", "world"];

type IsolatedObjectType isolated object {
    int a;
    string[] b;
};

isolated class IsolatedClassOverridingMutableFieldsInIncludedIsolatedObject {
    *IsolatedObjectType;

    final int a = 100;
    private string[] b;

    function init() {
        self.b = [];
    }

    function accessImmutableField() returns int => self.a + 1;

    isolated function accessMutableField() returns int {
        lock {
            self.b.push(...immutableStringArray);
            return self.b.length();
        }
    }
}

function testIsolatedObjectOverridingMutableFieldsInIncludedIsolatedObject() {
    isolated object {} isolatedObjectOverridingMutableFieldsInIncludedIsolatedObject = object IsolatedObjectType {

        final int a = 100;
        private string[] b = [];

        function accessImmutableField() returns int => self.a + 1;

        isolated function accessMutableField() returns int {
            lock {
                self.b.push(...immutableStringArray);
                return self.b.length();
            }
        }
    };
}

isolated class IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b;

    private int c;
    private map<int[]> d;

    isolated function init(record {int i;} & readonly b, int c, map<int[]> d) {
        self.b = b;
        self.c = c;
        self.d = d.clone();
    }

    isolated function accessMutableFieldInLockOne() returns int[] {
        int i = 1;

        lock {
            self.c = i;

            int j = i;
            self.c = i;

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    function accessMutableFieldInLockTwo() returns int[] {
        int i = 1;
        int[] x;

        lock {
            int j = i;

            lock {
                int[] y = [1, 2, 3];
                x = y.clone();
                self.c = i;
            }

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    isolated function accessImmutableFieldOutsideLock() returns int[] {
        int[] arr = [];

        arr.push(...self.a);
        arr[5] = self.b.i;
        return arr;
    }
}

isolated object {} isolatedObjectWithMethodsAccessingPrivateMutableFieldsWithinLockStatements = object {
    public final int[] & readonly a = [10, 20, 30];
    final readonly & record {int i;} b = {i: 101};

    private int c = 1;
    private map<int[]> d = {};

    function accessMutableFieldInLockOne() returns int[] {
        int i = 1;

        lock {
            self.c = i;

            int j = i;
            self.c = i;

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    isolated function accessMutableFieldInLockTwo() returns int[] {
        int i = 1;
        int[] x;

        lock {
            int j = i;

            lock {
                int[] y = [1, 2, 3];
                x = y.clone();
                self.c = i;
            }

            map<int[]> k = {
                a: [1, 2, i, j, i + j],
                b: self.a
            };
            self.d = k;
            k = self.d;
        }

        return self.a;
    }

    isolated function accessImmutableFieldOutsideLock() returns int[] {
        int[] arr = [];

        arr.push(...self.a);
        arr[5] = self.b.i;
        return arr;
    }
};

isolated class IsolatedClassWithNonPrivateIsolatedObjectFields {
    final isolated object {} a = object {
        final int i = 1;
        private map<int> j = {};
    };
    final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements b;
    private final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements c = new ({i: 1}, 102, {});
    private int[] d = [1, 2];

    function init() {
        self.b = new ({i: 1}, 102, {});
    }

    isolated function accessNonPrivateIsoltedObjectFieldOutsideLock() {
        int[] x = self.b.accessMutableFieldInLockOne();
    }
}

isolated object {} isolatedObjectWithNonPrivateIsolatedObjectFields = object {
    final isolated object { function foo() returns int; } a = object {
        final int i = 1;
        private map<int> j = {};

        isolated function foo() returns int {
            lock {
                return self.i + (self.j["a"] ?: 1);
            }
        }
    };
    final int|IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements b = new ({i: 1}, 102, {});
    private final IsolatedClassWithMethodsAccessingPrivateMutableFieldsWithinLockStatements c = new ({i: 1}, 102, {});
    private int[] d = [1, 2];

    function accessNonPrivateIsoltedObjectFieldOutsideLock() {
        int x = self.a.foo();
    }
};

const INT = 100;

isolated class IsolatedClassWithUniqueInitializerExprs {
    private map<int> a = <map<int>> {};
    final int[] & readonly b = [1, INT];
    private table<record {readonly int i; string name;}> c;

    isolated function init(table<record {readonly int i; string name;}> tb) {
        self.c = tb.clone();
    }
}

isolated object {} isolatedObjectWithUniqueInitializerExprs = object {
    private map<int> a = <map<int>> {};
    final int[] & readonly b = [1, INT];
    private table<record {readonly int id; string name;}> c;

    isolated function init() {
        self.c = table [
            {id: INT, name: "foo"},
            {id: INT + 100, name: "bar"}
        ];
    }
};

isolated class IsolatedClassWithValidCopyInInsideBlock {
    private string[] uniqueGreetings = [];

    isolated function add(string[] greetings) {
        lock {
            if self.uniqueGreetings.length() == 0 {
                self.uniqueGreetings = greetings.clone();
            }
        }
    }
}
