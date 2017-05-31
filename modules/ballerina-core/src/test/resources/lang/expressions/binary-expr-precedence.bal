function binaryOrExprWithLeftMostSubExprTrue(boolean one, boolean two, boolean three) (boolean) {
    json j = {};
    return one || ((string)j.isPresent == "test");
}


function binaryANDExprWithLeftMostSubExprFalse(boolean one, boolean two, boolean three) (boolean) {
    json j = {};
    return one && ((string)j.isPresent == "test");
}


function multiBinaryORExpr(boolean one, boolean two, boolean three) (int) {
    if ( one || two || three) {
       return 101;
    } else {
       return 201;
    }
}

function multiBinaryANDExpr(boolean one, boolean two, boolean three) (int) {
    if ( one && two && three) {
       return 101;
    } else {
       return 201;
    }
}
