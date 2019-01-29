function divideJson () {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 / j2;
}

function divideIncompatibleTypes () {
    string str;
    // Following line is invalid.
    str = "foo" / 3.0;
}
