function stringTemplateWithText() returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{firstName + " " + string `{{lastName}}`}} !!!`;
    return s;
}

function spaceBetweenBraces() returns (string) {
    string name = "Ballerina";
    string s = string `Hello {{name} }`;
    return s;
}
