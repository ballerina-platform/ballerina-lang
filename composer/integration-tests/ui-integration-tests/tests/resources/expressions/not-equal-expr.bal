function checkIntEquality(int a, int b) (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkFloatEquality(float a, float b) (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkBooleanEquality(boolean a, boolean b) (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkStringEquality(string a, string b) (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkFloatAndDoubleEquality(float a, double b) (boolean) {
    return (a == b);
}