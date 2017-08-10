function stringTemplate() (string) {
    json name = {};
    string s = string `Hello {{name}}`;
    return s;
}
