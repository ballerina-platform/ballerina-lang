import ballerina/io;

public function main() {

    // Defines the type of `a` as a pair that consists of an `int` and a `string`.
    [int, string] a = [10, "John"];
    io:println(a);

    int aint;
    string astr;
    // Defines a tuple of variable names on the left with a variable reference of which the type is a tuple.
    // The assignment statement assigns values of the tuple on the right to the variables on the left.
    // In Ballerina, this is referred to as tuple destructuring.
    [aint, astr] = a;
    io:println(aint);
    io:println(astr);

    // You can declare and assign values with `var` like this.
    var [aint1, astr1] = a;

    // Invokes a function that returns a tuple.
    var [q, r] = divideBy10(6);
    io:println("06/10: ", "quotient=", q, " remainder=", r);

    // To ignore a value in a tuple, use `_`.
    // This ignores the second return value.
    var [q1, _] = divideBy10(57);
    io:println("57/10: ", "quotient=", q1);

    [int, int] returnValue = divideBy10(9);
    // This ignores the first value of a tuple.
    var [_, r1] = returnValue;
    io:println("09/10: ", "remainder=", r1);
}

// This function returns a tuple of two integers.
function divideBy10(int d) returns ([int, int]) {
    int q = d / 10;
    int r = d % 10;
    return [q, r];
}
