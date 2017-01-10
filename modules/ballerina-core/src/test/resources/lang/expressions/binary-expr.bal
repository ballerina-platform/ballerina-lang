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