function inttolong(int value)(long) {
    long result;
    result = value;
    return result;
        }

function inttofloat(int value)(float) {
    float result;
    result = value;
    return result;
}

function inttodouble(int value)(double) {
    double result;
    result = value;
    return result;
}

function longtofloat(long value)(float) {
    float result;
    result = value;
    return result;
}

function longtodouble(long value)(double) {
    double result;
    result = value;
    return result;
}

function floattodouble(float value)(double) {
    double result;
    result = value;
    return result;
}

function booleanToIntImplicit(boolean b) (int) {
    int i = b;
    return i;
}

function booleanToFloatImplicit(boolean b) (float) {
    float f = b;
    return f;
}

function booleanToIntExplicit(boolean b) (int) {
    int i = (int) b;
    return i;
}

function booleanToFloatExplicit(boolean b) (float) {
    float f = (float) b;
    return f;
}

function intToBooleanExplicit(int i) (boolean) {
    boolean b = (boolean) i;
    return b;
}

function floatToBooleanExplicit(float f) (boolean) {
    boolean b = (boolean) f;
    return b;
}
