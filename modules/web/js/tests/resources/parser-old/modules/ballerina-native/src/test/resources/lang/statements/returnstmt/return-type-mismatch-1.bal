function testInputTypeMismatch(string s) (string, int, string){
    return split(s);
}

function split(string s) (string, string, string) {
    return s, "", s;
}