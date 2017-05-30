package lang.identifierliteral.pkg.main;

import lang.identifierliteral.pkg.var;

function getVarsInOtherPkg() (int, string, float, any) {
    return var:|Variable Int|, var:|Variable String|, var:|Variable Float|, var:|Variable Any|;
}

function accessStructWithIL()(string, int) {
    return var:|person 1|.|first name|, var:|person 1|.|current age|;
}