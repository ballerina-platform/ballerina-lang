
import globalvar.pkg.variable;

public float glbVarFloat = <float> variable:glbVarAnyInt;

public any glbVarAny = getAnyGlobalVar();

public int glbVarInt = variable:getIntValue();

function getGlobalVars() returns (int, string, float, any) {
    return (variable:glbVarInt, variable:glbVarString, variable:glbVarFloat, variable:glbVarAny);
}

function changeGlobalVar(int addVal) returns (float) {
    variable:glbVarFloatChange = 77 + <float> addVal;
    float value = variable:glbVarFloatChange;
    return value;
}

function getGlobalFloatVar() returns (float) {
    _ = changeGlobalVar(3);
    return variable:glbVarFloatChange;
}

function getAssignedGlobalVarFloat() returns (float) {
    return glbVarFloat;
}

function getAnyGlobalVar() returns (any) {
    float val = 45545;
    return val;
}

function getGlobalVarAny() returns (any) {
    return glbVarAny;
}

function getGlobalVarInt() returns (int) {
    return glbVarInt;
}

