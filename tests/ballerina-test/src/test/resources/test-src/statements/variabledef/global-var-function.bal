int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;

json glbVarJson;

float glbVarFloatLater;

function getGlobalVars() returns (int, string, float, any) {
    return (glbVarInt, glbVarString, glbVarFloat, glbVarAny);
}

function accessGlobalVar() returns int {
    int value;
    value = check <int>glbVarAny;
    return (glbVarInt + value);
}

function changeGlobalVar(int addVal) returns float {
    glbVarFloatChange = 77 + <float> addVal;
    float value = glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() returns float {
    _ = changeGlobalVar(3);
    return glbVarFloatChange;
}

function getGlobalVarFloat1() returns float {
    return glbVarFloat1;
}

function initializeGlobalVarSeparately() returns (json, float) {
    glbVarJson = {"name" : "James", "age": 30};
    glbVarFloatLater = 3432.3423;
    return (glbVarJson, glbVarFloatLater);
}