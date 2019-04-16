function stringTemplate1() returns (string) {
    string s = string `Hello ${name}`;
    return s;
}

function stringTemplate2() returns (string) {
    json name = {};
    string s = string `Hello ${name}`;
    return s;
}
