function intToFloatExplicit(int i) returns float {
    float f = <float> i;
    return f;
}

function floatToIntCast(float f) returns int {
    int x = <int>f;
    return x;
}

function decimalToIntCast(decimal d) returns int {
    int x = <int>d;
    return x;
}
