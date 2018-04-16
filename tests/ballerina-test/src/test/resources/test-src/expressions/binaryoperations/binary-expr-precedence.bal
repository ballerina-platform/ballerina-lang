function binaryOrExprWithLeftMostSubExprTrue(boolean one, boolean two, boolean three) returns (boolean) {
    return one || getBoolean();
}


function binaryANDExprWithLeftMostSubExprFalse(boolean one, boolean two, boolean three) returns (boolean) {
    return one && getBoolean();
}


function multiBinaryORExpr(boolean one, boolean two, boolean three) returns (int) {
    if ( one || two || three) {
        return 101;
    } else {
        return 201;
    }
}

function multiBinaryANDExpr(boolean one, boolean two, boolean three) returns (int) {
    if ( one && two && three) {
        return 101;
    } else {
        return 201;
    }
}

function getBoolean() returns (boolean ) {
    json j = {};
    string val = "ss";
    val = check <string>j.isPresent;
    return (val == "test");
}
