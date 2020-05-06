import ballerina/io;

public function main(string... args) {
    testIfElse();
    testWhileLoop();
}

function getName() returns string {
    return "rasika";
}

function testIfElse() {
    int i =10;
    if (i == 10) {
        io:println("true");
    } else if (i == 20) {
        io:println("true");
    } else {
        io:println("false");
    }
}

function testWhileLoop() {
    int i = 0;
    i = i + 1;
    // This is a basic `while` loop.
    while (i < 3) {
        io:println(i);
        i = i + 1;
    }

    int j = 0;
    while (j < 5) {
        io:println(j);
        j = j + 1;

        // The `break` statement can be used to break the loop.
        if (j == 3) {
            break;
        }
    }

    int k = 0;
    while (k < 5) {
        // The `continue` statement can be used to move to the
        // next loop iteration immediately.
        if (k < 3) {
            k = k + 1;
            continue;
        }

        io:println(k);
        k = k + 1;
    }
}
