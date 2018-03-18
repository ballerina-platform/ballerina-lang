
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
    |global json| = {"name" : "James", "age": 30};
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
    string |\| !#[{]} " \u2324| = "sample value";
    return |\| !#[{]} " \u2324|;
}

function testFunctionNameWithIL()(string) {
    return |test function for identifier|("sample");
}

function |test function for identifier|(string val) (string) {
    string s = " test";
    return val + s;
}

//connector |Test Connector|(string param1, string param2, int param3) {
//    boolean action2Invoked;
//
//    action action1() (string) {
//        string |sample string| = "this is a sample";
//        return |sample string|;
//    }
//
//    action |second action|() (string){
//        string |string \| value| = "sample string";
//       return |string \| value|;
//    }
//}

//function testConnectorNameWithIL() (string) {
//    endpoint<|Test Connector|> testConnector {
//        create |Test Connector|("MyParam1", "MyParam2", 5);
//    }
//    string value;
//
//    value = testConnector.action1();
//    return value;
//}

//function testConnectorActionWithIL() (string) {
//    endpoint<|Test Connector|> |test Connector| {
//        create |Test Connector|("MyParam1", "MyParam2", 5);
//    }
//    string value;
//
//    value = |test Connector|.|second action|();
//    return value;
//}

function useILInStructName() (string, string, int, string) {
    |family person| |person one| = {|first name|: "Tom", |last name|:"hank", |current age|: 50};
    return |person one|.|first name|, |person one|.|last name|, |person one|.|current age|, |person one|["first name"];
}

struct |family person| {
    string |first name|;
    string |last name|;
    int |current age|;
}

function testUnicodeInIL() (string) {
    string |සිංහල වචනය| = "සිංහල වාක්‍යක්";
    return |සිංහල වචනය|;
}

function testAcessILWithoutPipe() (string, string) {
     string |x| = "hello";
     return |x|, x;
 }
 
 function testAcessJSONFielAsIL() (json) {
     json j = {"foo" : {"int" : "I am an integer"}};
     return j.foo.|int|;
 }
 