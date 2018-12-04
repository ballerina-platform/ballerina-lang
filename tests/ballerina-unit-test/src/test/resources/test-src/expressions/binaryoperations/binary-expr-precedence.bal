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

function getBoolean() returns (boolean) {
    json j = {};
    var val = string.convert(j.isPresent);
    if (val is string) {
        return (val == "test");
    } else {
        panic val;
    }
}
