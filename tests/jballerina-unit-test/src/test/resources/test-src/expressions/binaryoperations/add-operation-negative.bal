function addJson() {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 + j2;
}

function addIncompatibleTypes() {
    int i;
    // Following line is invalid.
    i = 5 + "abc";
}
