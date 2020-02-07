

function valueAssignmentAndRetrieval() returns (int) {
    int[] x = [3];
    int[] y = [4, 5];

    int[][] xx = [x, y];
    int retVal = arrayTest(xx);
    return xx[0][0];
}

function arrayInitializationAndRetrieval() returns (int) {
    int[][] x = [];
    x[0] = [];
    x[0][0] = 1;

    return x[0][0];
}

function threeDarray() returns (int) {
    int[] x = [1, 2];
    int[] y = [3, 4];

    int[][] xx = [x, y];

    int[][][] xxx = [xx];

    return xxx[0][0][1];
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

    int lngth = sss.length();
    return a;
}

type A record {
    string name = "";
};

// Super Type
type Person record {
    string name = "";
};

// Assignable to Person type
type Employee record {
    string name = "";
    boolean intern = false;
};

Person[] personArray = [];
Employee[] employeeArray = [];

function testValidArrayAssignment() {
    personArray = employeeArray;
    Employee e =  <Employee> personArray[0];
}

function testCovarianceBooleanOrFloatOrRecordArray() {
    (boolean|float)?[] x = [true, 2.0, true, 15.0];
    (boolean|float)?[] y = x;
}

function testFiniteTypeArrayFill4(int index) returns Employee[] {
    Employee[] ar = [];
    return ar;
}