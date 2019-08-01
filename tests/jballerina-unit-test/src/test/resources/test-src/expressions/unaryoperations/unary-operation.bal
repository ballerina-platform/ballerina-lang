function negativeIntTest() returns [int, int] {
    int x;
    int y;
    x = -5;
    y = -x;

    return [x,y];
}

function positiveIntTest() returns [int, int] {
    int x;
    int y;
    x = +5;
    y = +x;

    return [x,y];
}

function negativeFloatTest() returns [float, float] {
    float x;
    float y;
    x = -5.0;
    y = -x;

    return [x,y];
}

function positiveFloatTest() returns [float, float] {
    float x;
    float y;
    x = +5.0;
    y = +x;

    return [x,y];
}

function booleanNotTest() returns [boolean, boolean, boolean] {
    boolean x;
    boolean y;
    boolean z;
    x = false;
    y = !x;
    z = !false;

    return [x,y,z];
}

function unaryExprInIfConditionTest() returns (boolean) {
    boolean x;
    x = false;
    if(!x) {
        return true;
    } else {
        return false;
    }
}

function unaryNegationTest(int a, int b) returns (int) {
    return a-(-b);
}

function unaryPositiveNegationTest(int a) returns (int) {
    return +-a;
}