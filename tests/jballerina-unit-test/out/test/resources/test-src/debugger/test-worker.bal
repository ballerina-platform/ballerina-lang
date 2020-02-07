import ballerina/io;
public function main(string... args) {
    simpleWorkers();
    io:println("worker run finished");
}

function simpleWorkers() {
    worker w1 {
        int p = 15;
        int q = 5;
        // Invoke Some random Logic.
        int a = calculateExp1(p , q);
        io:println("worker 1 - " + a);
        a -> w2;
        a = <- w2;
    }
    worker w2 {
        int a = 0;
        int p = 15;
        int q = 5;
        // Invoke Some random Logic.
        int b = calculateExp3(p , q);
        io:println("worker 2 - " + b);
        a = <- w1;
        b -> w1;
    }
}

function calculateExp1(int x, int y) returns (int) {
    int z = 0;
    int a = y;
    while(x >= a) {
        a = a + 1;
        if(a == 10) {
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}

function calculateExp3(int x, int y) returns (int) {
    int z = 0;
    int a = y;
    while(x >= a) {
        a = a + 1;
        if(a == 10) {
            z = 100;
            break;
        }
        z = z + 10;
    }
    return z;
}