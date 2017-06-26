int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;

json glbVarJson;

float glbVarFloatLater;

function getGlobalVars() (int, string, float, any) {
    return glbVarInt, glbVarString, glbVarFloat, glbVarAny;
}

function accessGlobalVar() (int) {
    int value;
    value, _ = (int)glbVarAny;
    return glbVarInt + value;
}

function changeGlobalVar(int addVal) (float) {
    glbVarFloatChange = 77 + <float> addVal;
    float value = glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() (float) {
    changeGlobalVar(3);
    return glbVarFloatChange;
}

function getGlobalVarFloat1()(float) {
    return glbVarFloat1;
}

function initializeGlobalVarSeparately()(json, float) {
    glbVarJson = {"name" : "James", "age": 30};
    glbVarFloatLater = 3432.3423;
    return glbVarJson, glbVarFloatLater;
}