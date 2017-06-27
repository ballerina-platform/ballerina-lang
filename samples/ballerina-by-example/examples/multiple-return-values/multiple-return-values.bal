import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"Here's a function which returns 2 int values."}
function divideBy10 (int d) (int, int) {
    return d / 10, d % 10;
}

function main (string[] args) {
    int q;
    int r;
    //Multiple variable assignment
    q, r = divideBy10(24);
    system:println("24/10: " + "quotient=" + q + " " +
                   "reminder=" + r);

    //To ignore a particular return value in a multiple assignment statement, use '_'.
    q, _ = divideBy10(57);
    system:println("57/10: " + "quotient=" + q);

    _, r = divideBy10(9);
    system:println("05/10: " + "reminder=" + r);

    //Multiple assignment with 'var' allows you to define the variable then and there.
    //Variable type is inferred from the right-hand side.
    var q1, r1 = divideBy10(102);
    system:println("24/10: " + "quotient=" + q1 + " " +
                   "reminder=" + r1);
}

