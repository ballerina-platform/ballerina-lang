import ballerina/io;

function main (string[] args) {

    int i = 0;
    //This is a basic while loop.
    while (i < 3) {
        io:println(i);
        i = i + 1;
    }

    int j = 0;
    while (j < 5) {
        io:println(j);
        j = j + 1;

        //Use the break statement if you want to break the loop.
        if (j == 3) {
            break;
        }
    }

    int k = 0;
    while (k < 5) {
        //Sample usage of the next statement.
        if (k < 3) {
            k = k + 1;
            next;
        }

        io:println(k);
        k = k + 1;
    }
}
