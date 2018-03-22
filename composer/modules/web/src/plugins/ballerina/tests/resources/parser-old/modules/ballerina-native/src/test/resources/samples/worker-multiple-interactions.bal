import ballerina/lang.system;

function testMultiInteractions(int k)(int q) {
    int x = 1100;

    x -> w1;
    x <- w3;

    system:println(x);
    return x;

    worker w1 {
        int x;
        x <- default;
        x = x + 1;
        system:println(x);
        x -> w2;
    }

    worker w2 {
        int x;
        x <- w1;
        x = x + 1;
        system:println(x);
        x -> w3;
    }

    worker w3 {
        int x;
        x <- w2;
        x = x + 1;
        system:println(x);
        x -> default;
    }

}

