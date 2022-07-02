// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

isolated function testNonIsolatedStartActionWithNonIsolatedFuncCallExpr() {
    future<()> _ = start f1();

    int[] & readonly arr = [1, 2, 3];
    future<int> _ = start f4(arr);
}

int[] intVal = [];

isolated function testNonIsolatedStartActionWithNonIsolatedArgsInFuncCallExpr() {
    int[] arr = [1, 2, 3];
    future<int> _ = start f4(arr);

    future<int> _ = start f5(arr);

    future<int> _ = start f6(arr, intVal);
}

function f1() {
}

function f4(int[] a) returns int {
    return 1;
}

function f5(int[] a) returns int {
    return 1;
}

function f6(int[] a, int[]... b) returns int {
    return 1;
}

type ObjectType1 object {
    int[] a;
    isolated function f1() returns int;
};

type ObjectType2 object {
    int[] & readonly a;
    function f1() returns int;
};

type ObjectType3 object {
    int[] a;
    function f1(int a, int[] b, int[]... c) returns int;
};

isolated function testNonIsolatedStartActionWithNonIsolatedObjectExprInMethodCallExpr() {
    var obj1 = object {
        int[] a = [1, 2, 3];

        isolated function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj1.f1();

    ObjectType1 obj2 = object {
        int[] a = [1, 2, 3];

        isolated function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj2.f1();

    object {
        int[] a;
        isolated function f1() returns int;
    } obj3 = object {
        int[] a = [1, 2, 3];

        isolated function f1() returns int {
            return 50;
        }
    };
    future<int> _ = start obj3.f1();
}

isolated function testNonIsolatedStartActionWithNonIsolatedMethodCallExpr() {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];

        function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj1.f1();

    ObjectType2 obj2 = object {
        final int[] & readonly a = [1, 2, 3];

        function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj2.f1();

    object {
        int[] a;
        function f1() returns int;
    } obj3 = object {
        int[] a = [1, 2, 3];

        function f1() returns int {
            return 50;
        }
    };
    future<int> _ = start obj3.f1();
}

int val = 0;

isolated function testNonIsolatedStartActionWithNonIsolatedArgsInMethodCallExpr() {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];

        function f1(int a, int[] b, int[]... c) returns int {
            return 10;
        }
    };
    int[] arr = [1, 2, 3];
    future<int> _ = start obj1.f1(val, arr, arr);

    ObjectType3 obj2 = object {
        int[] a = [1, 2, 3];

        function f1(int a, int[] b, int[]... c) returns int {
            return 10;
        }
    };
    future<int> _ = start obj2.f1(1, arr, arr);

    object {
        int[] a;
        function f1(int a, int[] b, int[]... c) returns int;
    } obj3 = object {
        int[] a = [1, 2, 3];

        function f1(int a, int[] b, int[]... c) returns int {
            return 50;
        }
    };
    future<int> _ = start obj3.f1(val, arr, arr);
}

type ObjectType4 client object {
    int[] a;
    isolated remote function f1() returns int;
};

type ObjectType5 client object {
    int[] & readonly a;
    remote function f1() returns int;
};

type ObjectType6 client object {
    int[] a;
    remote function f1(int a, int[] b, int[]... c) returns int;
};

isolated function testNonIsolatedStartActionWithNonIsolatedObjectExprInClientRemoteMethodCallExpr() {
    var obj1 = client object {
        int[] a = [1, 2, 3];

        isolated remote function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj1->f1();

    ObjectType4 obj2 = client object {
        int[] a = [1, 2, 3];

        isolated remote function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj2->f1();

    client object {
        int[] a;
        isolated remote function f1() returns int;
    } obj3 = client object {
        int[] a = [1, 2, 3];

        isolated remote function f1() returns int {
            return 50;
        }
    };
    future<int> _ = start obj3->f1();
}

isolated function testNonIsolatedStartActionWithNonIsolatedClientRemoteMethodCallExpr() {
    var obj1 = client object {
        final int[] & readonly a = [1, 2, 3];

        remote function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj1->f1();

    ObjectType5 obj2 = client object {
        final int[] & readonly a = [1, 2, 3];

        remote function f1() returns int {
            return 10;
        }
    };
    future<int> _ = start obj2->f1();

    client object {
        int[] a;
        remote function f1() returns int;
    } obj3 = client object {
        int[] a = [1, 2, 3];

        remote function f1() returns int {
            return 50;
        }
    };
    future<int> _ = start obj3->f1();
}

isolated function testNonIsolatedStartActionWithNonIsolatedArgsInClientRemoteMethodCallExpr() {
    var obj1 = client object {
        final int[] & readonly a = [1, 2, 3];

        remote function f1(int a, int[] b, int[]... c) returns int {
            return 10;
        }
    };
    int[] arr = [1, 2, 3];
    future<int> _ = start obj1.f1(val, arr, arr);

    ObjectType6 obj2 = client object {
        int[] a = [1, 2, 3];

        remote function f1(int a, int[] b, int[]... c) returns int {
            return 10;
        }
    };
    future<int> _ = start obj2.f1(1, arr, arr);

    client object {
        int[] a;
        remote function f1(int a, int[] b, int[]... c) returns int;
    } obj3 = client object {
        int[] a = [1, 2, 3];

        remote function f1(int a, int[] b, int[]... c) returns int {
            return 50;
        }
    };
    future<int> _ = start obj3.f1(val, arr, arr);
}

isolated function testStartActionWithStrandAnnotation() {
    int[] arr = [];
    future<int> _ = @strand{thread:"any"} start f4(arr);

    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];

        isolated function f1() returns int {
            return 10;
        }
    };
    future<int> _ = @strand{thread:"any"} start obj1.f1();

    var obj2 = client object {
        final int[] & readonly a = [1, 2, 3];

        remote function f1(int[] a) returns int {
            return 10;
        }
    };
    future<int> _ = @strand{thread:"any"} start obj2->f1(arr);
}
