function testWhileStmt(int x, int y) returns (int) {
    int z;
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
            values[i -1] =check <float>args[i];
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