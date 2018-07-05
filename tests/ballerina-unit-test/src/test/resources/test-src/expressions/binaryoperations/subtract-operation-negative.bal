function incompatibleSubtract () {
    float f;
    // Following line is invalid.
    f = 5 - "foo";
}

function sunstractJson () {
    json j1;
    json j2;
    json j3;
    j1 = {"name":"Jack"};
    j2 = {"state":"CA"};
    // Following line is invalid.
    j3 = j1 - j2;
}
