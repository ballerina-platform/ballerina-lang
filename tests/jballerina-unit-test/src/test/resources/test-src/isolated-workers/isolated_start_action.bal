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

isolated function testIsolatedStartActionWithFunctionCallExprWithNoArgs() returns error? {
    future<()> a1 = start f1();
    () v1 = checkpanic wait a1;
    assertEquality((), v1);

    future<string> a2 = start f2();
    string v2 = checkpanic wait a2;
    assertEquality("ABCD", v2);
}

isolated function f1() {
}

isolated function f2(string d = "D", string... e) returns string {
    return "ABC" + d + (e.length() > 0 ? e[0] : "");
}

final float floatVal = 25.5;

final int[] & readonly intArr = [1, 2, 3];

isolated function testIsolatedStartActionWithFunctionCallExprWithIsolatedArgs() returns error? {
    future<float> a1 = start f3(12, 30.5);
    float v1 = checkpanic wait a1;
    assertEquality(42.5, v1);

    int[] & readonly arr = [1, 2, 3];
    future<int> a2 = start f4(arr);
    int v2 = checkpanic wait a2;
    assertEquality(1, v2);

    future<int> a3 = start f5(arr);
    int v3 = checkpanic wait a3;
    assertEquality(3, v3);

    future<int> a4 = start f6(arr, ["A", "B"], [1, 2, 3]);
    int v4 = checkpanic wait a4;
    assertEquality(5, v4);

    var obj = isolated object {
        final int a = 10;
    };
    future<int> a5 = start f7(obj);
    int v5 = checkpanic wait a5;
    assertEquality(10, v5);

    future<int> a6 = start f7(object {
        final int a = 10;
    });
    int v6 = checkpanic wait a6;
    assertEquality(10, v6);

    future<float> a7 = start f3(intArr[0], floatVal);
    float v7 = checkpanic wait a7;
    assertEquality(26.5, v7);

    future<int> a8 = start f5(intArr);
    int v8 = checkpanic wait a8;
    assertEquality(3, v8);
}

function g1() returns int {
    return 2;
}

isolated function f3(int a, float b) returns float {
    return <float>a + b;
}

isolated function f4(int[] a) returns int {
    return a[0];
}

isolated function f5(int[] a, int[] b = []) returns int {
    return a[2];
}

isolated function f6(int[] a, string[] b = [], int[]... c) returns int {
    return a[2] + c[0][1];
}

isolated function f7(object {int a;} obj) returns int {
    return obj.a;
}

type ObjectType1 readonly & object {
    int[] & readonly a;
    string b;
    isolated function f1() returns int;
};

type ObjectType2 isolated object {
    int[] & readonly a;
    string b;
    isolated function f1() returns int;
};

isolated function testIsolatedStartActionWithMethodCallExprWithNoArgs() returns error? {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 10;
        }
    };
    future<int> a1 = start obj1.f1();
    int v1 = checkpanic wait a1;
    assertEquality(10, v1);

    ObjectType1 obj2 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 20;
        }
    };
    future<int> a2 = start obj2.f1();
    int v2 = checkpanic wait a2;
    assertEquality(20, v2);

    ObjectType2 obj3 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 30;
        }
    };
    future<int> a3 = start obj3.f1();
    int v3 = checkpanic wait a3;
    assertEquality(30, v3);

    var obj4 = isolated object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 40;
        }
    };
    future<int> a4 = start obj4.f1();
    int v4 = checkpanic wait a4;
    assertEquality(40, v4);

    object {
        int[] & readonly a;
        string b;
        isolated function f1() returns int;
    } & readonly obj5 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1() returns int {
            return 50;
        }
    };
    future<int> a5 = start obj5.f1();
    int v5 = checkpanic wait a5;
    assertEquality(50, v5);
}

final int intVal = 25;

type ObjectType3 readonly & object {
    int[] & readonly a;
    string b;
    isolated function f1(int a, int[] b) returns int;
    isolated function f2(int a, int[] b = [10, 20]) returns int;
};

type ObjectType4 isolated object {
    int[] & readonly a;
    string b;
    isolated function f1(int a, int[] b) returns int;
    isolated function f2(int a, int... b) returns int;
};

isolated function testIsolatedStartActionWithMethodCallExprWithIsolatedArgs() returns error? {
    var obj1 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1(int a, int[] b) returns int {
            return a + b[0];
        }
    };
    int[] & readonly arr = [1, 2, 3];
    future<int> a1 = start obj1.f1(1, arr);
    int v1 = checkpanic wait a1;
    assertEquality(2, v1);

    future<int> a2 = start obj1.f1(intVal, [1, 2, 3]);
    int v2 = checkpanic wait a2;
    assertEquality(26, v2);

    future<int> a3 = start obj1.f1(f6(arr, ["A", "B"], [1, 2, 3]), intArr);
    int v3 = checkpanic wait a3;
    assertEquality(6, v3);

    ObjectType3 obj2 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }

        isolated function f2(int a, int[] b = [10, 20]) returns int {
            return a + self.a[0] + b[0];
        }
    };
    future<int> a4 = start obj2.f1(1, arr);
    int v4 = checkpanic wait a4;
    assertEquality(3, v4);

    future<int> a5 = start obj2.f1(intVal, [1, 2, 3]);
    int v5 = checkpanic wait a5;
    assertEquality(27, v5);

    future<int> a6 = start obj2.f1(intVal, intArr);
    int v6 = checkpanic wait a6;
    assertEquality(27, v6);

    future<int> a7 = start obj2.f2(1);
    int v7 = checkpanic wait a7;
    assertEquality(12, v7);

    future<int> a8 = start obj2.f2(intVal, arr);
    int v8 = checkpanic wait a8;
    assertEquality(27, v8);

    ObjectType4 obj3 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }

        isolated function f2(int a, int... b) returns int {
            return a + self.a[0] + b[0];
        }
    };
    future<int> a9 = start obj3.f1(1, arr);
    int v9 = checkpanic wait a9;
    assertEquality(3, v9);

    future<int> a10 = start obj3.f1(intVal, [10, 20]);
    int v10 = checkpanic wait a10;
    assertEquality(36, v10);

    future<int> a11 = start obj3.f1(intVal, intArr);
    int v11 = checkpanic wait a11;
    assertEquality(27, v11);

    future<int> a12 = start obj3.f2(intVal, f6(arr, ["A", "B"], [1, 2, 3]));
    int v12 = checkpanic wait a12;
    assertEquality(31, v12);

    future<int> a13 = start obj3.f2(intVal, 12, 13, 14);
    int v13 = checkpanic wait a13;
    assertEquality(38, v13);

    var obj4 = isolated object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1(int a, int[] b, int... c) returns int {
            return a + self.a[0] + b[0] + (c.length() > 1 ? c[1] : 0);
        }
    };
    future<int> a14 = start obj4.f1(1, [1, 2, 3]);
    int v14 = checkpanic wait a14;
    assertEquality(3, v14);

    future<int> a15 = start obj4.f1(12, arr, f6(arr, ["A", "B"], [1, 2, 3]), intArr[0]);
    int v15 = checkpanic wait a15;
    assertEquality(15, v15);

    object {
        int[] & readonly a;
        string b;
        isolated function f1(int a, int[] b) returns int;
    } & readonly obj5 = object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }
    };
    future<int> a16 = start obj5.f1(f6(arr, ["A", "B"], [1, 2, 3]), arr);
    int v16 = checkpanic wait a16;
    assertEquality(7, v16);

    future<int> a17 = start obj5.f1(intVal, intArr);
    int v17 = checkpanic wait a17;
    assertEquality(27, v17);
}

type ObjectType5 readonly & client object {
    int[] & readonly a;
    string b;
    isolated remote function f1() returns int;
};

type ObjectType6 isolated client object {
    int[] & readonly a;
    string b;
    isolated remote function f1() returns int;
};

isolated function testIsolatedStartActionWithClientRemoteMethodCallActionWithNoArgs() returns error? {
    var obj1 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1() returns int {
            return 10;
        }
    };
    future<int> a1 = start obj1->f1();
    int v1 = checkpanic wait a1;
    assertEquality(10, v1);

    ObjectType5 obj2 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1() returns int {
            return 20;
        }
    };
    future<int> a2 = start obj2->f1();
    int v2 = checkpanic wait a2;
    assertEquality(20, v2);

    ObjectType6 obj3 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1() returns int {
            return 30;
        }
    };
    future<int> a3 = start obj3->f1();
    int v3 = checkpanic wait a3;
    assertEquality(30, v3);

    var obj4 = isolated client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1() returns int {
            return 40;
        }
    };
    future<int> a4 = start obj4->f1();
    int v4 = checkpanic wait a4;
    assertEquality(40, v4);

    client object {
        int[] & readonly a;
        string b;
        isolated remote function f1() returns int;
    } & readonly obj5 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1() returns int {
            return 50;
        }
    };
    future<int> a5 = start obj5->f1();
    int v5 = checkpanic wait a5;
    assertEquality(50, v5);
}

type ObjectType7 readonly & client object {
    int[] & readonly a;
    string b;
    isolated remote function f1(int a, int[] b) returns int;
    isolated remote function f2(int a, int[] b = [10, 20]) returns int;
};

type ObjectType8 isolated client object {
    int[] & readonly a;
    string b;
    isolated remote function f1(int a, int[] b) returns int;
    isolated remote function f2(int a, int... b) returns int;
};

isolated function testIsolatedStartActionWithClientRemoteMethodCallActionWithIsolatedArgs() returns error? {
    var obj1 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1(int a, int[] b) returns int {
            return a + b[0];
        }
    };
    int[] & readonly arr = [1, 2, 3];
    future<int> a1 = start obj1->f1(1, arr);
    int v1 = checkpanic wait a1;
    assertEquality(2, v1);

    future<int> a2 = start obj1->f1(intVal, [1, 2, 3]);
    int v2 = checkpanic wait a2;
    assertEquality(26, v2);

    future<int> a3 = start obj1->f1(f6(arr, ["A", "B"], [1, 2, 3]), intArr);
    int v3 = checkpanic wait a3;
    assertEquality(6, v3);

    ObjectType7 obj2 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }

        isolated remote function f2(int a, int[] b = [10, 20]) returns int {
            return a + self.a[0];
        }
    };
    future<int> a4 = start obj2->f1(1, arr);
    int v4 = checkpanic wait a4;
    assertEquality(3, v4);

    future<int> a5 = start obj2->f1(intVal, [1, 2, 3]);
    int v5 = checkpanic wait a5;
    assertEquality(27, v5);

    future<int> a6 = start obj2->f1(intVal, intArr);
    int v6 = checkpanic wait a6;
    assertEquality(27, v6);

    future<int> a7 = start obj2->f2(1);
    int v7 = checkpanic wait a7;
    assertEquality(2, v7);

    future<int> a8 = start obj2->f2(intVal, arr);
    int v8 = checkpanic wait a8;
    assertEquality(26, v8);

    ObjectType8 obj3 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }

        isolated remote function f2(int a, int... b) returns int {
            return a + self.a[0] + b[0];
        }
    };
    future<int> a9 = start obj3->f1(1, arr);
    int v9 = checkpanic wait a9;
    assertEquality(3, v9);

    future<int> a10 = start obj3->f1(intVal, [10, 20]);
    int v10 = checkpanic wait a10;
    assertEquality(36, v10);

    future<int> a11 = start obj3->f1(intVal, intArr);
    int v11 = checkpanic wait a11;
    assertEquality(27, v11);

    future<int> a12 = start obj3->f2(intVal, f6(arr, ["A", "B"], [1, 2, 3]));
    int v12 = checkpanic wait a12;
    assertEquality(31, v12);

    future<int> a13 = start obj3->f2(intVal, 12, 13, 14);
    int v13 = checkpanic wait a13;
    assertEquality(38, v13);

    var obj4 = isolated client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1(int a, int[] b, int... c) returns int {
            return a + self.a[0] + b[0] + (c.length() > 1 ? c[1] : 0);
        }
    };
    future<int> a14 = start obj4->f1(1, [1, 2, 3]);
    int v14 = checkpanic wait a14;
    assertEquality(3, v14);

    future<int> a15 = start obj4->f1(12, arr, f6(arr, ["A", "B"], [1, 2, 3]), intArr[0]);
    int v15 = checkpanic wait a15;
    assertEquality(15, v15);

    client object {
        int[] & readonly a;
        string b;
        isolated remote function f1(int a, int[] b) returns int;
    } & readonly obj5 = client object {
        final int[] & readonly a = [1, 2, 3];
        final string b = "A";

        isolated remote function f1(int a, int[] b) returns int {
            return a + self.a[0] + b[0];
        }
    };
    future<int> a16 = start obj5->f1(f6(arr, ["A", "B"], [1, 2, 3]), arr);
    int v16 = checkpanic wait a16;
    assertEquality(7, v16);

    future<int> a17 = start obj5->f1(intVal, intArr);
    int v17 = checkpanic wait a17;
    assertEquality(27, v17);
}

const ASSERTION_ERROR_REASON = "AssertionError";

isolated function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
