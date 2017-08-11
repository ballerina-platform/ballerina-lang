function booleanToIntImplicit(boolean b) (int) {
    int i = <int>b;
    return i;
}

function booleanToFloatImplicit(boolean b) (float) {
    float f = <float>b;
    return f;
}

function booleanToIntExplicit(boolean b) (int) {
    int i = <int> b;
    return i;
}

function booleanToFloatExplicit(boolean b) (float) {
    float f = <float> b;
    return f;
}

function intToBooleanExplicit(int i) (boolean) {
    boolean b = <boolean> i;
    return b;
}

function floatToBooleanExplicit(float f) (boolean) {
    boolean b = <boolean> f;
    return b;
}
