string s = "Ballerina";
const string name = s;
public const string name2 = s;

int a = 10;
const age = a;
public const age2 = a;

const x = 10;
const int y = 20;

function testAssignment() {
    x = 1;
    y = 2;
}

function testWorkerInteractions() {
    worker w1 {
        x <- w2;
    }
    worker w2 {
        30 -> w1;
    }
}

// -------------------------------------

function testMixedWithFinal() returns int {
    L l = 10;
    return 0;
}

type L M|N|O;

final var M = "m";

type N int;

final string O = "o";

// -------------------------------------

int aa = 10;

aa bb = 2;

const cc = 10;

cc dd = 2;
