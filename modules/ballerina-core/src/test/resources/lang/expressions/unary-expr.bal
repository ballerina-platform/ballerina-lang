function negativeIntTest() (int, int) {
    int x;
    int y;
    x = -5;
    y = -x;

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