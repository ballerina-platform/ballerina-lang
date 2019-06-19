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

        // If you want to break the loop, use the `break` statement like this.
        if (j == 3) {
            break;
        }
    }

    int k = 0;
    while (k < 5) {
        // If you want to move to the next loop iteration immediately, use the `continue` statement liek this.
        if (k < 3) {
            k = k + 1;
            continue;
        }

        io:println(k);
        k = k + 1;
    }
}
