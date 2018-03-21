function stringTemplateWithText15() returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{firstName + " " + string `{{lastName}}`}} !!!`;
    return s;
}
