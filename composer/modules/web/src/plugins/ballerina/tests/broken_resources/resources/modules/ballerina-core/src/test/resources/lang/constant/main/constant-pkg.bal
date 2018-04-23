
import lang.constant.variable;

const int constNegativeInt = -342;

const int constNegativeIntWithSpace = -     88;

const float constNegativeFloat = -88.2;

const float constNegativeFloatWithSpace = -      3343.88;

float glbVarFloat = variable:constFloat;

function accessConstantFromOtherPkg() (float) {
    return variable:constFloat;
}

function assignConstFromOtherPkgToGlobalVar()(float) {
    return glbVarFloat;
}

function getNegativeConstants()(int, int, float, float) {
    return constNegativeInt, constNegativeIntWithSpace, constNegativeFloat, constNegativeFloatWithSpace;
}


const float a = 4;

function floatIntConversion() (float, float, float){
    float[] f = [1,2,3,4,5,6,8];
    return a, f[5], 10;
}

