function checkIntEquality(int a, int b) returns (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkFloatEquality(float a, float b) returns (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkBooleanEquality(boolean a, boolean b) returns (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}

function checkStringEquality(string a, string b) returns (int) {
    int returnType;
    if (a == b) {
        returnType = 1;
    } else if (a != b) {
        returnType = 2;
    }

    return returnType;
}
