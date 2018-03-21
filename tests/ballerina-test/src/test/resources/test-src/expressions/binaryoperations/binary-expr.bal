function makeChild(boolean stone, boolean value) returns (boolean) {
    boolean result;
    // stone and valuable
    if (stone && value) {
        result = true;
        // same as above
    } else if (value && stone) {
        result = true;
    } else if (value || !stone) {
        result =  false;
        // not stone or valuable
    } else if (!stone || !value) {
        result =  false;
    } else if (stone || !value) {
        result =  false;
    }
    return result;
}

function multiBinaryANDExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return one && two && three;
}

function multiBinaryORExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return one || two || three;
}

function multiBinaryExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return (!one || (two && three)) || (!three || (one && two));
}