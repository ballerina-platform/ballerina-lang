import variable;

final int constNegativeInt = -342;

final int constNegativeIntWithSpace = -     88;

final float constNegativeFloat = -88.2;

final float constNegativeFloatWithSpace = -      3343.88;

float glbVarFloat = variable:getConstFloat();

function accessConstantFromOtherPkg() returns (float) {
    return variable:getConstFloat();
}

function assignConstFromOtherPkgToGlobalVar() returns (float) {
    return glbVarFloat;
}

function getNegativeConstants() returns [int, int, float, float] {
    return [constNegativeInt, constNegativeIntWithSpace, constNegativeFloat, constNegativeFloatWithSpace];
}


final float a = 4.0;

function floatIntConversion() returns [float, float, float]{
    float[] f = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0];
    return [a, f[5], 10.0];
}

function accessPublicConstantFromOtherPackage() returns string {
    return variable:name;
}

function accessPublicConstantTypeFromOtherPackage() returns variable:AB {
    return variable:A;
}

type AB "A";

function testTypeAssignment() returns AB {
    AB ab = variable:A; // This is valid because this is equivalant to `AB ab = "A";`.
    return ab;
}
