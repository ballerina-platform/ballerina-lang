import ballerina/runtime;

int i = getInt();

function getInt() returns int{
    worker w1 {
        return 1;
    }
    worker w2 {
        runtime:sleep(10000);
    }
}

function test() returns int{
    return i;
}
