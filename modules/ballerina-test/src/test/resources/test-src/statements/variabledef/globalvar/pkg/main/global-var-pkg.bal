package globalvar.pkg.main;

import globalvar.pkg.variable;

public float glbVarFloat = <float> variable:glbVarAnyInt;

public any glbVarAny = getAnyGlobalVar();

public int glbVarInt = variable:getIntValue();

function getGlobalVars() (int, string, float, any) {
    return variable:glbVarInt, variable:glbVarString, variable:glbVarFloat, variable:glbVarAny;
}


function changeGlobalVar(int addVal) (float) {
    variable:glbVarFloatChange = 77 + <float> addVal;
    float value = variable:glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() (float) {
    _ = changeGlobalVar(3);
    return variable:glbVarFloatChange;
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

