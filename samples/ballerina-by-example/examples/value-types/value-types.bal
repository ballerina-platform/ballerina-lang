import ballerina.lang.system;

function main (string[] args) {
    //Variables defined without an initial value are zero-valued. For example, the zero value for an int is 0.
    int i;
    system:println(i);

    float f = 20.0;
    system:println(f);

    //The zero value of a string is "".
    string s;
    system:println(s);

    boolean b = true;
    system:println(b);

    // Ballerina infers type from the initial value. This is same as "int k = 5";
    var k = 5;
    system:println( 10 + k);
}

