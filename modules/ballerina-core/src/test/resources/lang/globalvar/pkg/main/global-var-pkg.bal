package lang.globalvar.pkg.main;

import lang.globalvar.pkg.var;

float glbVarFloat = (int)var:glbVarAny;

any glbVarAny = getAnyGlobalVar();

int glbVarInt = var:getIntValue();

function getGlobalVars() (int, string, float, any) {
    return var:glbVarInt, var:glbVarString, var:glbVarFloat, var:glbVarAny;
}


function changeGlobalVar(int addVal) (float) {
    var:glbVarFloatChange = 77 + addVal;
    float value = var:glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() (float) {
    changeGlobalVar(3);
    return var:glbVarFloatChange;
}

function getAssignedGlobalVarFloat()(float) {
    return glbVarFloat;
}

function getAnyGlobalVar() (any) {
    float val = 45545;
    return val;
}

function getGlobalVarAny()(any) {
    return glbVarAny;
}

function getGlobalVarInt()(int) {
    return glbVarInt;
}

