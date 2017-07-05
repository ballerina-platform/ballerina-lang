import ballerina.lang.system;

function main (string[] args) {
    //Variables defined without an initial value are zero-valued. For example, the zero value for an int value type is 0.
    int i;
    system:println(i);
    
    //This is how you define a float value type.
    float f = 20.0;
    system:println(f);

    //The zero value of a string is "".
    string s;
    system:println(s);

    //This is how you define a boolean value type.
    boolean b = true;
    system:println(b);
}

