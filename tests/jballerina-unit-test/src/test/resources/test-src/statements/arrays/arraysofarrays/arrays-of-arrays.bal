function valueAssignmentAndRetrieval() returns (int) {
    int[] x = [3];
    int[] y = [4, 5];

    int[][] xx = [x, y];
    return xx[0][0];
}

function arrayInitializationAndRetrieval() returns (int) {
    int[][] x = [];
    x[0] = [];
    x[0][0] = 1;

    return x[0][0];
}

function arrayToArrayAssignment() returns (int) {
    int[] x;
    x = [9];

    int[][] xx = [];
    xx[0] = x;

    return xx[0][0];
}

function threeDarray() returns (int) {
    int[] x = [1, 2];
    int[] y = [3, 4];

    int[][] xx = [x, y];

    int[][][] xxx = [xx];

    return xxx[0][0][1];
}

function threeDarrayValueAccess() returns (int) {
    int[][][] xxx;
    xxx = [];

    xxx[0] = [];
    xxx[0][0] = [];
    xxx[0][0][0] = 99;

    return xxx[0][0][0];
}

function threeDarrayStringValueAccess() returns (string) {
    string[][][] xxx;
    xxx = [];

    xxx[0] = [];
    xxx[0][0] = [];
    xxx[0][0][0] = "string";

    return xxx[0][0][0];
}

function twoDarrayFunctionCalltest() returns (int) {
    int[][] xx = [];
    xx[0] = [];
    xx[0][1] = 4;

    return arrayTest(xx);
}

function arrayTest(int[][] yy) returns (int) {
    return yy[0][1];
}

type B record {
    int x;
};

function twoDarrayStructTest() returns (int) {
    B b1 = {x:1};
    B b2 = {x:2};

    B[] x1 = [b1, b2];
    B[] x2 = [b2, b1];
    B[][] xx = [x1, x2];

    B b3 = xx[0][1];

    return b3.x;
}

function nestedArrayInit() returns [int, int] {
    int[][][] a = [[[100, 200, 3], [2, 5, 6]], [[100, 200, 3], [2, 5, 6], [12, 15, 16]]];
    return [a[1][2][0], a[0][1][2]];
}

function testStringArrayIterator() returns string {
    string[][][] sss = [];
    string[][] ss = [];
    string[] s = [];
    s[3] = "B";
    ss[2] = s;
    sss[5] = ss;

    string str = "";

    foreach var s1 in sss {
        foreach var s2 in ss {
            foreach var s3 in s2 {
                str = str + s3;
            }
        }
    }

    return str;
}

function testIntArrayIterator() returns int {
    int[][][] sss = [];
    int[][] ss = [];
    int[] s = [];
    s[3] = 7;
    ss[2] = s;
    sss[5] = ss;

    int a = 3;

    foreach var s1 in sss {
        foreach var s2 in ss {
            foreach var s3 in s2 {
                a = a + s3;
            }
        }
    }

    return a;
}

function testFloatArrayIterator() returns float {
    float[][][] sss = [];
    float[][] ss = [];
    float[] s = [];
    s[3] = 4.0;
    ss[2] = s;
    sss[5] = ss;

    float f = 3.1;

    foreach var s1 in sss {
        foreach var s2 in ss {
            foreach var s3 in s2 {
                f = f + s3;
            }
        }
    }

    return f;
}



function testByteArrayIterator() returns byte[] {
    byte[][][][] sss = [];
    byte[][][] ss = [];
    byte[][] s = [];
    s[3] = base16 `aa`;
    ss[2] = s;
    sss[5] = ss;

    byte[] a;
    foreach var s1 in sss {
        foreach var s2 in ss {
            foreach var s3 in s2 {
                a = s3;
            }
        }
    }

    return a;
}

function testRefArrayIterator() returns A {
    A[][][] sss = [];
    A[][] ss = [];
    A[] s = [];
    s[3] = {name:"ballerina"};
    ss[2] = s;
    sss[5] = ss;

    A a;
    foreach var s1 in sss {
        foreach var s2 in ss {
            foreach var s3 in s2 {
                a = s3;
            }
        }
    }
    return a;
}

type A record {
    string name = "";
};

function testArrayUnion() returns [boolean[][], string[][]] {
    (int[]|boolean[][]) x1 = [[false]];
    (string[]|string[][]) x2 = [["scope1"]];
    return [<boolean[][]>x1, <string[][]>x2];
}

type Foo1 object {
    string fooId1;
};

type Foo2 object {
    string fooId2;
};

class Bar {
    *Foo1;
    *Foo2;

    public function init() {
        self.fooId1 = "Foo1";
        self.fooId2 = "Foo2";
    }
}

function testObjectArrayUnion() returns Foo1[][] {
    Bar b = new;
    Foo1[]|Foo1[][] arr1 = [[b]];
    Foo1[]|Foo2[][] arr2 = [[b]];
    Foo1[]|Bar[][] arr3 = [[b]];
    Bar[]|Bar[][] arr4 = [[b]];
    Bar[]|Foo1[][] arr5 = [[b]];

    Foo1 f = new Bar();
    Foo1[]|Foo1[][] arr6 = [[f]];
    Foo1[]|Bar[][] arr7 = [[b]];
    Bar[]|Bar[][] arr8 = [[b]];
    Bar[]|Foo1[][] arr9 = [[b]];

    Foo1[]|Foo1[][]|Foo2[][] arr10 = [[f]];
    if (arr3 is Bar[][]) {
        Foo1[][] f1 = arr3;
        Foo2[][] f2 = arr3;
    }
    return <Foo1[][]>arr1;
}
