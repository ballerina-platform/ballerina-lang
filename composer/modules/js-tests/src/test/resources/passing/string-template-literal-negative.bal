function stringTemplate1() (string) {
    string s = string `Hello {{name}}`;
    return s;
}

function stringTemplate2() (string) {
    json name = {};
    string s = string `Hello {{name}}`;
    return s;
}
