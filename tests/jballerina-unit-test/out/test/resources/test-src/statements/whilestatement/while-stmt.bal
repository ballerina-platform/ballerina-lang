import ballerina/lang.'float as floats;

function testWhileStmt(int x, int y) returns (int) {
    int z = 0;
    int a = y;

    while(x >= a) {
        a = a + 1;
        z = z + 10;
    }
    return z;
}

function testWhileScope(int number) returns (int) {
    int i = number;
    while(i < 4) {
        i = i + 1;
        if(i == 2) {
            int x = 200;
            i = x;
        } else {
            int x = 400;
            i = x;
        }
    }
    return i;
}

function testWhileScopeWithIf() returns [int, float] {
    float[] values = [];
    string operator;
    float sum = 0.0;
    string[] args = ["+" , "10", "20"];

    int i = 0;
    while (i < 3) {
        if(i == 0){
            operator = args[0];
        } else {
            var val = floats:fromString(args[i]);
            if (val is error) {
                 panic val;
            } else {
                 values[i -1] = val;
            }
        }
        i = i + 1;
    }
    int j = 0;
    while(j < 2) {
        sum = sum + values[j];
        j = j + 1;
    }
    return [j, sum];
}

function testWhileStmtWithoutBraces(int x, int y) returns (int) {
    int z = 0;
    int a = y;

    while x >= a {
        a = a + 1;
        z = z + 10;
    }
    return z;
}

function testWhileStmtWithDefaultValues() returns [int, string, float] {
    int count = 0;
    int fi = 0;
    string fs = "";
    float ff = 0.0;
    while(count <3) {
        int i = 0;
        string s = "";
        float f = 0.0;
        i += 1;
        f += 1.0;
        s += "hello";
        fi = i;
        fs = s;
        ff = f;
        count += 1;
    }
    return [fi, fs, ff];
}

function testNestedWhileWithBreak1() returns string {
    string result = "";
    while (true) {
        while (false) {
        }
        result = result + "inner";
        break;
    }
    return result;
}

function testNestedWhileWithBreak2() returns string {
    string result = "";
    while (true) {
        while (true) {
            while (true) {
                while (false) {
                }
                result = result + "level3";
                break;
            }
            result = result + "level2";
            break;
        }
        result = result + "level1";
        break;
    }
    return result;
}

function testWhileWithContinue() returns string {
    string result = "";
    int a = 1;
    while (a > 0) {
        if (a == 5) {
            break;
        }
        a += 1;
        if (a == 3) {
            continue;
        }
        result = result + "inner" + a.toString();
    }
    return result;
}

function testNestedWhileWithContinue() returns string {
    string result = "";
    int a = 2;
    while (a > 0) {
        if (a == 5) {
            break;
        }
        while (a > 1) {
            while (a > 2) {
                if (a == 4) {
                    break;
                }
                result = result + "level3";
                a += 1;
            }
            if (a == 4) {
                break;
            }
            result = result + "level2";
            a += 1;
        }
        if (a == 4) {
            a += 1;
            result = result + "level1";
            continue;
        }
        result = result + "level0";
    }
    return result;
}

function testTypeNarrowingInWhileBody() returns string {
    record {| string val; |}?[] arr = [{val: "foo1"}, {val: "foo2"}, {val: "foo3"}, (), {val: "foo5"}];

    record {| string val; |}|() rec = arr[0];
    int i = 0;
    string result = "";

    while (rec is record {| string val; |}) {
        result += rec.val;
        i += 1;
        rec = arr[i];
    }

    return result;
}
