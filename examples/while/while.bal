import ballerina/io;

public function main() {
    int i = 0;
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
