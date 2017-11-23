function binaryOrExprWithLeftMostSubExprTrue(boolean one, boolean two, boolean three) (boolean) {
    return one || getBoolean();
}


function binaryANDExprWithLeftMostSubExprFalse(boolean one, boolean two, boolean three) (boolean) {
    json j = {};
    return one && getBoolean();
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

function getBoolean()(boolean ) {
    json j = {};
    string val;
    val, _ = (string)j.isPresent;
    return (val == "test");
}
