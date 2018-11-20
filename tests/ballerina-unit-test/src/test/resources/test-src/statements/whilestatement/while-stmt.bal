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

function testWhileScopeWithIf() returns (int, float) {
    float[] values = [];
    string operator;
    float sum = 0.0;
    string[] args = ["+" , "10", "20"];

    int i = 0;
    while (i < 3) {
        if(i == 0){
            operator = args[0];
        } else {
            var val = <float>args[i];
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
    return (j, sum);
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

function testWhileStmtWithDefaultValues() returns (int, string, float) {
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
    return (fi, fs, ff);
}
