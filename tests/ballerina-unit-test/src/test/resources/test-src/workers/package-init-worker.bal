import ballerina/runtime;

int i = getInt();

function getInt() returns int {
    worker w1 returns int {
        return 1;
    }
    worker w2 {
        runtime:sleep(10000);
    }
    return wait w1;
}

function test() returns int {
    return i;
}
