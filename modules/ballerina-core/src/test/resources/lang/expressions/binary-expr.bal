function makeChild(boolean stone, boolean value) (boolean) {
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