
const float |const IL| = 88.90;

string |global var| = "this is a IL with global var";

json |global json|;

function getGlobalVarWithIL() (string) {
    return |global var|;
}

function getConstWithIL() (float) {
    return |const IL|;
}

function defineAndGetIL() (string, float, int) {
    string |local string var| = |global var|;
    float |local float var| = |const IL|;
    int |local int var| = 99934;
    return |local string var|, |local float var|, |local int var|;
}

function useILWithinStruct() (string, string, int) {
    Person person = {|first name|: "Tom", |last name|:"hank", |current age|: 50};
    return person.|first name|, person.|last name|, person.|current age|;
}

struct Person {
    string |first name|;
    string |last name|;
    int |current age|;
}

function useILInStructVar() (string, string, int) {
    Person |person 1| = {|first name|: "Harry", |last name|:"potter", |current age|: 25};
    return |person 1|.|first name|, |person 1|.|last name|, |person 1|.|current age|;
}

function useILAsrefType()(json) {
    |global json| = `{"name" : "James", "age": 30}`;
    return |global json|;
}

function useILAsArrayIndex() (float) {
    float[] |float array| = [234, 8834.834,88];
    int |array index| = 1;
    return |float array|[|array index|];
}

function passILValuesToFunction() (string, int) {
    string |first name| = "Bill";
    string |last name| = "Kary";
    int age = 40;
    return passILValuesAsParams(|first name|, |last name|, age);
}

function passILValuesAsParams(string |first name|, string |last name|, int |current age|) (string, int) {
    string |full name| = |first name| + " " + |last name|;
    return |full name|, |current age|;
}

function testCharInIL() (string) {
    string |\| !#[{]}\u2324| = "sample value";
    return |\| !#[{]}\u2324|;
}

