package lang.constant.main;

import lang.constant.var;

float glbVarFloat = var:constFloat;

function accessConstantFromOtherPkg() (float) {
    return var:constFloat;
}

function assignConstFromOtherPkgToGlobalVar()(float) {
    return glbVarFloat;
}

