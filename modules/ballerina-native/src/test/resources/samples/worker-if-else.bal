import ballerina.lang.system;

function testWorkerInteractionInCondition(int k)(int q) {
    int x = 5;

    x -> w1;
    x <- w1;

    system:println(x);
    return x;

    worker w1 {
        int x;
        x <- default;
        x = x + 1;
        if (x == 1) {
            x -> default;
        } else {
            x -> default;
        }
    }

}