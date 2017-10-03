function stringTemplate1() (string) {
    string s = string `Hello {{name}}`;
    return s;
}

function stringTemplate2() (string) {
    json name = {};
    string s = string `Hello {{name}}`;
    return s;
}

function stringTemplateWithText15() (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{firstName + " " + string `{{lastName}}`}} !!!`;
    return s;
}
