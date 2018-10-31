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
