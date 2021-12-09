function stringTemplateWithText1 () returns (string) {
    string s = string `${"`"}`;
    return s;
}

function stringTemplateWithText2 () returns (string) {
    string s = string `\\`;
    return s;
}

function stringTemplateWithText3 () returns (string) {
    string s = string `\{`;
    return s;
}

function stringTemplateWithText4 () returns (string) {
    string s = string `{{`;
    return s;
}

function stringTemplateWithText5 () returns (string) {
    string s = string `$\{`;
    return s;
}

function stringTemplateWithText6 () returns (string) {
    string s = string `}`;
    return s;
}

function stringTemplateWithText7 () returns (string) {
    string s = string `}}`;
    return s;
}

function stringTemplateWithText8 () returns (string) {
    string s = string `}}}`;
    return s;
}

function stringTemplateWithText9 () returns (string) {
    string s = string `Hello`;
    return s;
}

function stringTemplateWithText10 () returns (string) {
    string name = "Ballerina";
    string s = string `${name}`;
    return s;
}

function stringTemplateWithText11 () returns (string) {
    string name = "Ballerina";
    string s = string `Hello ${name}`;
    return s;
}

function stringTemplateWithText12 () returns (string) {
    string name = "Ballerina";
    string s = string `${name} !!!`;
    return s;
}

function stringTemplateWithText13 () returns (string) {
    string name = "Ballerina";
    string s = string `Hello ${name} !!!`;
    return s;
}

function stringTemplateWithText14 () returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello ${lastName}, ${firstName}`;
    return s;
}

function stringTemplateWithText15 () returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello ${lastName}, ${firstName} !!!`;
    return s;
}

function stringTemplateWithText16 () returns (string) {
    int count = 10;
    string s = string `Count = ${count}`;
    return s;
}

function stringTemplateWithText17 () returns (string) {
    string s = string `$\{count}`;
    return s;
}

function stringTemplateWithText18 () returns (string) {
    int count = 10;
    string s = string `\\${count}`;
    return s;
}

function stringTemplateWithText19 () returns (string) {
    string path = "root";
    string s = string `Path = \\${path}`;
    return s;
}

function stringTemplateWithText20 () returns (string) {
    string s = string `Path = \\`;
    return s;
}

function stringTemplateWithText21 () returns (string) {
    string firstName = "John";
    string lastName = "Smith";
    string s = string `Hello ${firstName + " " + lastName} !!!`;
    return s;
}

function stringTemplateWithText22 () returns (string) {
    string s = string `Hello ${getFullName()} !!!`;
    return s;
}

function stringTemplateWithText23 () returns (string) {
    string s = string `Hello ${getFirstName() + " " + getLastName()} !!!`;
    return s;
}

function getFullName () returns (string) {
    return getFirstName() + " " + getLastName();
}

function getFirstName () returns (string) {
    return "John";
}

function getLastName () returns (string) {
    return "Smith";
}

function emptyStringTemplate () returns (string) {
    string s = string ``;
    return s;
}

function concatStringTemplateExprs() returns (string) {
    string s1 = "John";
    string s2 = "Doe";
    return string `FirstName: ${s1}.` + string ` Second name: ${s2}`;
}

function stringTemplateEscapeChars() returns (string) {
    string s = string `\n\r\b\t\f\'\"${"`"}\{\\`;
    return s;
}

function stringTemplateStartsWithDollar() returns (string) {
    string s = string `$$$$ A ${4 + 4} B`;
    return s;
}

function stringTemplateEndsWithDollar() returns (string) {
    string s = string `A ${4 + 4} B $$$$`;
    return s;
}

function stringTemplateWithOnlyDollar() returns (string) {
    string s = string `$$$$$$$$$`;
    return s;
}

function stringTemplateDollarFollowedByEscapedLeftBrace() returns (string) {
    string s = string `Hi $$$$\{ ${5 * 5} End`;
    return s;
}

function stringTemplateDollarFollowedByRightBrace() returns (string) {
    string s = string `Hi $$$$}}} ${5 * 5} End`;
    return s;
}

function stringTemplateWithBraces() returns (string) {
    string s = string `{{{{4 + 4}}}}}}}}}}`;
    return s;
}

function complexStringTemplateExpr() returns (string) {
    string s1 = "Ballerina";
    return string `Hello \n$\\$$\{Dummy\tText\${"`"}\\test ${s1} endText\\{{{{{innerStartText ${4 + 3} }}!!!`;
}

public type Foo int|float|decimal|string|boolean;

function stringTemplateExprWithUnionType1() returns string {
    Foo foo = 4;
    return string`${foo}`;
}

function stringTemplateExprWithUnionType2() returns string {
    Foo foo = 4.5;
    return string`${foo}`;
}

function stringTemplateExprWithUnionType3() returns string {
    Foo foo = "chirans";
    return string`${foo}`;
}

function stringTemplateExprWithUnionType4() returns string {
    Foo foo = true;
    return string`${foo}`;
}

function testStringTemplateExprWithUnionType() {
    assertEquality("4", stringTemplateExprWithUnionType1());
    assertEquality("4.5", stringTemplateExprWithUnionType2());
    assertEquality("chirans", stringTemplateExprWithUnionType3());
    assertEquality("true", stringTemplateExprWithUnionType4());
}

function testNumericEscapes() {
        string s1 = string `\u{61}`;
        string s2 = string `\u{61}ppl\\u{65}`;
        string s3 = string `A \u{5C} B`;
        string s4 = string `A \\u{5C} B`;

        assertEquality("\\u{61}", s1);
        assertEquality("[92,117,123,54,49,125]", s1.toBytes().toString());
        assertEquality("\\u{61}ppl\\\\u{65}", s2);
        assertEquality("A \\u{5C} B", s3);
        assertEquality("A \\\\u{5C} B", s4);
}

type Type1 1|2|3;
type Type2 "int"|"string"|Type1;
type Type3 1|"hello"|Type2;
type Type4 2.0|3.0|5.0;

function testStringTemplateWithFiniteType() {
    Type1 a = 2;
    Type2 b = 3;
    Type2 c = "string";
    Type3 d = "hello";
    Type4 e = 2.0;

    string str = string `${a}`;
    assertEquality("2", str);

    str = string `${b}`;
    assertEquality("3", str);

    str = string `${c}`;
    assertEquality("string", str);

    str = string `${d}`;
    assertEquality("hello", str);

    str = string `${e}`;
    assertEquality("2.0", str);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
