import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"These names are treated as variables defined at the top of the function."}
function divideBy10 (int d) (int quotient, int remainder) {
    return d / 10, d % 10;
}

@doc:Description {value:"If the return statement does not contain any argument then the named return values will be returned."}
function divideBy5 (int d) (int quotient, int remainder) {
    quotient = d / 10;
    remainder = d % 10;
    return;
}

@doc:Description {value:"Named return variables are treated as local variables and will be initialized to their zero value."}
function getDefaultValues () (int a, float b,
                              boolean c, string d) {
    return a, b, c, d;
}


function main (string... args) {
    var q1, r1 = divideBy10(93);
    system:println("93/10: " + "quotient=" + q1 + " " +
                   "remainder=" + r1);

    var q2, r2 = divideBy5(93);
    system:println("93/5: " + "quotient=" + q2 + " " +
                   "remainder=" + r2);

    int a;
    float b;
    boolean c;
    string d;
    a, b, c, d = getDefaultValues();

    system:println("a: " + a);
    system:println("b: " + b);
    system:println("c: " + c);
    system:println("d: " + d);
}
