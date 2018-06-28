import ballerina/io;

function main(string... args) {

    // Here, you can define the type of `a` as a pair that consists of an int and a string.
    (int, string) a = (10, "John");
    io:println(a);

    // This has a tuple of variable names to the left, and a variable referent of which the type is a tuple.
    // This assignment statement assigns values of the tuple in the right to the variables in the left.
    // In Ballerina, this is referred to as tuple destructuring.
    int aint;
    string astr;
    (aint, astr) = a;
    io:println(aint);
    io:println(astr);

    // You can declare and assign values with `var` as shown here.
    var (aint1, astr1) = a;

    // This invokes a function that returns a tuple.
    var (q, r) = divideBy10(6);
    io:println("06/10: " + "quotient=" + q + " " + "remainder=" + r);

    //To ignore a return value, use '_'.
    //This ignores the second return value.
    var (q1, _) = divideBy10(57);
    io:println("57/10: " + "quotient=" + q1);

    //This ignores the first return value.
    var (_, r1) = divideBy10(9);
    io:println("09/10: " + "remainder=" + r1);
}

// This function returns a tuple of two integers.
function divideBy10(int d) returns (int, int) {
    int q = d / 10;
    int r = d % 10;
    return (q, r);
}
