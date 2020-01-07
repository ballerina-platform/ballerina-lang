function concatBMP() returns (any) {
    string prefix = "red ";
    string s = "apple";
    return prefix + s;
}

function nonBMPLength() returns (int) {
    string smiley = "h\u263Allo";
    return smiley.length();
}

