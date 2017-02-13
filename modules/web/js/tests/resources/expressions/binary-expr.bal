function makeChild(boolean stone, boolean value) (boolean) {
    boolean result;
    if (stone && value) {
        result = true;
    } else if (value && stone) {
        result = true;
    } else if (value || !stone) {
        result =  false;
    } else if (!stone || !value) {
        result =  false;
    } else if (stone || !value) {
        result =  false;
    }
    return result;
}

function multiBinaryANDExpression(boolean one, boolean two, boolean three) (boolean) {
    return one && two && three;
}

function multiBinaryORExpression(boolean one, boolean two, boolean three) (boolean) {
    return one || two || three;
}

function multiBinaryExpression(boolean one, boolean two, boolean three) (boolean) {
    return (!one || (two && three)) || (!three || (one && two));
}