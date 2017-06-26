import ballerina.lang.system;

function main (string[] args) {

    int i = 0;

    //This is a basic while loop
    while (i < 20) {

        system:println("This is number " + i);
        i = i + 1;

    //Use the break statement if you want to break the loop
        if (i == 10) {
            break;
        }

    }


}
