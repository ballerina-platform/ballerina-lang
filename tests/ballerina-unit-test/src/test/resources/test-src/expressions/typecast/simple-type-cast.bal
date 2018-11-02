function booleanToIntImplicit(boolean b) returns (int) {
    int i = <int>b;
    return i;
}

function booleanToFloatImplicit(boolean b) returns (float) {
    float f = <float>b;
    return f;
}

function booleanToIntExplicit(boolean b) returns (int) {
    int i = <int> b;
    return i;
}

function booleanToFloatExplicit(boolean b) returns (float) {
    float f = <float> b;
    return f;
}

function intToBooleanExplicit(int i) returns (boolean) {
    boolean b = <boolean> i;
    return b;
}

function floatToBooleanExplicit(float f) returns (boolean) {
    boolean b = <boolean> f;
    return b;
}

function intToFloatExplicit(int i) returns float {
    float f = <float> i;
    return f;
}
