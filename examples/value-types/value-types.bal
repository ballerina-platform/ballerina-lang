import ballerina/io;

function main (string[] args) {
    //Variables defined without an initial value are zero-valued. For example, the zero value for an int is 0.
    int i;
    io:println(i);

    float f = 20.0;
    io:println(f);

    //The default value of a string is and empty string "".
    string s;
    io:println(s);

    boolean b = true;
    io:println(b);
}
