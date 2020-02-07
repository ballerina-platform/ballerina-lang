function testIfStmt(int aa, int b, int c) returns [int, int] {

    int x;
    x = 10;

    int a1;
    if (aa == b) {
        a1 = 100;
    } else if (aa == b + 1){
        a1 = 200;
    } else {
        a1 = 400;
    }

    int b1 = c;

    return [a1 + x, b1 + 1];
}

function testMultipleTypeGuardsWithAndOperator() returns int {
    int|string x = 5;
    any y = 7;
    if (x is int && y is int) {
        return x + y;
    } else {
        return -1;
    }
}

type RecA record {
    string a;
};

type RecB record {
    string b;
    string c;
};

function testSimpleRecordTypes_1() returns string {
    RecA x = {a:"foo"};
    any y = x;
     if (y is RecA) {
        return y.a;
    } else if (y is RecB) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}