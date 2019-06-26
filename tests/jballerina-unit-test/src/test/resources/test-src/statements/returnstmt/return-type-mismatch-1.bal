function testInputTypeMismatch(string s) returns [string, int, string]{
    return split(s);
}

function split(string s) returns [string, string, string] {
    return [s, "", s];
}
