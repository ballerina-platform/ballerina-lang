import ballerina/runtime;

int i = getInt();

function getInt() returns int {
    @concurrent{}
    worker w1 returns int {
        return 1;
    }
    @concurrent{}
    worker w2 {
        runtime:sleep(10000);
    }
    return wait w1;
}

function test() returns int {
    return i;
}
