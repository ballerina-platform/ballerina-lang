package main;

import variable;

@final int constNegativeInt = -342;

@final int constNegativeIntWithSpace = -     88;

@final float constNegativeFloat = -88.2;

@final float constNegativeFloatWithSpace = -      3343.88;

float glbVarFloat = variable:constFloat;

function accessConstantFromOtherPkg() returns (float) {
    return variable:constFloat;
}

function assignConstFromOtherPkgToGlobalVar() returns (float) {
    return glbVarFloat;
}

function getNegativeConstants() returns (int, int, float, float) {
    return (constNegativeInt, constNegativeIntWithSpace, constNegativeFloat, constNegativeFloatWithSpace);
}


@final float a = 4;

function floatIntConversion() returns (float, float, float){
    float[] f = [1,2,3,4,5,6,8];
    return (a, f[5], 10.0);
}

