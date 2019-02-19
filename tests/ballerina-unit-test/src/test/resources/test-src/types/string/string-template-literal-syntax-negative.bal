function checkStringTemplateWithText() returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello ${firstName + " " + string `${lastName}} !!!`;
    return s;
}

function checkSpaceBetweenDollarAndBrace() returns (string) {
    string name = "Ballerina";
    string s = string `He\llo $ {name}}`;
    return s;
}
