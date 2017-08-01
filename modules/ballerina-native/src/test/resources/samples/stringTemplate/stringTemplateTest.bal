function emptyStringTemplate() (string) {
    string s = string ``;
    return s;
}

function stringTemplateWithText1() (string) {
    string s = string `Hello`;
    return s;
}

function stringTemplateWithText2() (string) {
    string name = "Ballerina";
    string s = string `{{name}}`;
    return s;
}

function stringTemplateWithText3() (string) {
    string name = "Ballerina";
    string s = string `Hello {{name}}`;
    return s;
}

function stringTemplateWithText4() (string) {
    string name = "Ballerina";
    string s = string `Hello {{name}} !!!`;
    return s;
}

function stringTemplateWithText5() (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{lastName}}, {{firstName}}`;
    return s;
}

function stringTemplateWithText6() (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{lastName}}, {{firstName}} !!!`;
    return s;
}

function stringTemplateWithText7() (string) {
    int count = 10;
    string s = string `Count = {{count}}`;
    return s;
}

function stringTemplateWithText8() (string) {
    string s = string `\{{count}}`;
    return s;
}

function stringTemplateWithText9() (string) {
    int count = 10;
    string s = string `\\{{count}}`;
    return s;
}

function stringTemplateWithText10() (string) {
    string path = "root";
    string s = string `Path = \\{{path}}`;
    return s;
}

function stringTemplateWithText11() (string) {
    string s = string `Path = \\`;
    return s;
}

function stringTemplateWithText12() (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello {{firstName + " " + lastName}} !!!`;
    return s;
}

function stringTemplateWithText13() (string) {
    string s = string `Hello {{getName()}} !!!`;
    return s;
}

function stringTemplateWithText14() (string) {
    string s = string `Hello {{getFirstName() + " " + getLastName()}} !!!`;
    return s;
}

function getName () (string ) {
    return "John Smith";
}

function getFirstName () (string ) {
    return "John";
}

function getLastName () (string ) {
    return "Smith";
}
