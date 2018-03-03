import ballerina.io;

@Description {value:"Here's a function which returns two int values."}
function divideBy10 (int d) (int, int) {
    return d / 10, d % 10;
}

function main (string[] args) {
    int q;
    int r;
    //Multiple variable assignment.
    q, r = divideBy10(24);
    io:println("24/10: " + "quotient=" + q + " " + "remainder=" + r);
}

