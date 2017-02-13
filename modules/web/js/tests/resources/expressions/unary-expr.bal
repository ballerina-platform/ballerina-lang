function negativeIntTest() (int, int) {
    int x;
    int y;
    x = -5;
    y = -x;

    return x,y;
}

function positiveIntTest() (int, int) {
    int x;
    int y;
    x = +5;
    y = +x;

    return x,y;
}

function negativeLongTest(long x) (long, long) {
    long y;
    x = -x;
    y = -x;

    return x,y;
}

function positiveLongTest(long x) (long, long) {
    long y;
    x = +x;
    y = +x;

    return x,y;
}

function negativeFloatTest() (float, float) {
    float x;
    float y;
    x = -5.0;
    y = -x;

    return x,y;
}

function positiveFloatTest() (float, float) {
    float x;
    float y;
    x = +5.0;
    y = +x;

    return x,y;
}

function negativeDoubleTest(double x) (double, double) {
    double y;
    x = -x;
    y = -x;

    return x,y;
}

function positiveDoubleTest(double x) (double, double) {
    double y;
    x = +x;
    y = +x;

    return x,y;
}

function booleanNotTest() (boolean, boolean, boolean) {
    boolean x;
    boolean y;
    boolean z;
    x = false;
    y = !x;
    z = !false;

    return x,y,z;
}

function unaryExprInIfConditionTest() (boolean) {
    boolean x;
    x = false;
    if(!x) {
        return true;
    } else {
        return false;
    }
}

function unaryNegationTest(int a, int b) (int) {
    return a--b;
}

function unaryPositiveNegationTest(int a) (int) {
    return +-a;
}