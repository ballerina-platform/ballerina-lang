import ballerina/lang.system;

function main (string... args) {

    int i = 0;
    //This is a basic while loop.
    while (i < 3) {
        system:println(i);
        i = i + 1;
    }

    int j = 0;
    while (j < 5) {
        system:println(j);
        j = j + 1;

        //Use the break statement if you want to break the loop.
        if (j == 3) {
            break;
        }
    }

    int k = 0;
    while (k < 5) {
        //Sample usage of the continue statement.
        if (k < 3) {
            k = k + 1;
            continue;
        }
                 
        system:println(k);
        k = k + 1;
    }
}
