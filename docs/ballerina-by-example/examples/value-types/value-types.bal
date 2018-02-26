import ballerina.io;

function main (string[] args) {
    //Variables defined without an initial value are zero-valued. For example, the zero value for an int is 0.
    int i;
    io:println(i);

    float f = 20.0;
    io:println(f);

    //The zero value of a string is null.
    string s;
    io:println(s);

    boolean b = true;
    io:println(b);
}
