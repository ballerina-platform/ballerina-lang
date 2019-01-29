import ballerina/io;

public function benchmarkStringContains() {
    string name = "BallerinaLang";
    boolean result = name.contains("Lang");
}

public function benchmarkStringEqualsIgnoreCase() {
    string test = "testString";
    boolean result = test.equalsIgnoreCase("ram");
}

public function benchmarkStringConcat() {
    string s1 = "John";
    string s2 = "Doe";
    string s3 = s2 + s2;
}

public function benchmarkStringHasPrefix() {
    string name = "randomPerson";
    string prefix = "Mr";
    boolean result = name.hasPrefix(prefix);
}

public function benchmarkStringHasSuffix() {
    string name = "randomPerson";
    string suffix = "Darwin";
    boolean result = name.hasSuffix(suffix);
}

public function benchmarkStringIndexOf() {
    string s = "randomPerson";
    string str = "Darwin";
    int result = s.indexOf(str);
}

public function benchmarkStringLastIndexOf() {
    string s = "randomPersonDarwincy";
    string str = "Darwin";
    int result = s.lastIndexOf(str);
}

public function benchmarkStringReplace() {
    string s = "This is a test for string";
    string source = "string";
    string target = "str";
    string str = s.replace(source, target);
}

public function benchmarkStringReplaceAll() {
    string s = "This is a test for string having string inside statement.";
    string source = "string";
    string target = "str";
    string str = s.replaceAll(source, target);
}

public function benchmarkStringReplaceFirst() {
    string s = "This is a test for string having string testing replace first string in statement.";
    string source = "string";
    string target = "str";
    string str = s.replaceFirst(source, target);
}

public function benchmarkStringSubstring() {
    string s = "This statment is to test substring function.";
    string str = s.substring(5, 16);
}

public function benchmarkStringToLower() {
    string s = "CONVERT TO LOWER CASE";
    string str = s.toLower();
}

public function benchmarkStringToUpper() {
    string s = "convert to upper case";
    string str = s.toUpper();
}

public function benchmarkStringTrim() {
    string s = " trim ";
    string str = s.trim();
}

public function benchmarkStringIntValueOf() {
    string five = <string>(5);
}

public function benchmarkStringFloatValueOf() {
    string floatNumber = <string>(777.777);
}

public function benchmarkStringBooleanValueOf() {
    string strTrue = <string>(true);
}

public function benchmarkStringValueOf() {
    string str = <string>("This is string.");
}

public function benchmarkXmlValueOf() {
    xml x = xml `<name>ram</name>`;
    string str = io:sprintf("%s", x);
}

public function benchmarkStringJsonValueOf() {
    json testJson = { "Hello": "World" };
    string str = testJson.toString();
}

public function benchmarkStringLength() {
    string str = "this is a test string";
    int i = str.length();
}

public function benchmarkStringUnescape() {
    string str = "This \\is an escaped \\String";
    string s = str.unescape();
}

public function benchmarkStringSplit() {
    string str = "This is a test string";
    string split = "test";
    string[] splittedString = str.split(split);
}
