
final float 'const\ IL = 88.90;

string 'global\ var = "this is a IL with global var";

json 'global\ json = {};

function getGlobalVarWithIL() returns (string) {
    return 'global\ var;
}

function getConstWithIL() returns (float) {
    return 'const\ IL;
}

function defineAndGetIL() returns [string, float, int] {
    string 'local\ string\ var = 'global\ var;
    float 'local\ float\ var = 'const\ IL;
    int 'local\ int\ var = 99934;
    return ['local\ string\ var, 'local\ float\ var, 'local\ int\ var];
}

function useILWithinStruct() returns [string, string, int] {
    Person person = {'first\ name: "Tom", 'last\ name:"hank", 'current\ age: 50};
    return [person.'first\ name, person.'last\ name, person.'current\ age];
}

type Person record {
    string 'first\ name;
    string 'last\ name;
    int 'current\ age;
};

function useILInStructVar() returns [string, string, int] {
    Person 'person\ 1 = {'first\ name: "Harry", 'last\ name:"potter", 'current\ age: 25};
    return ['person\ 1.'first\ name, 'person\ 1.'last\ name, 'person\ 1.'current\ age];
}

function useILAsrefType()returns (json) {
    'global\ json = {"name" : "James", "age": 30};
    return 'global\ json;
}

function useILAsArrayIndex() returns (float) {
    float[] 'float\ array = [234.0, 8834.834, 88.0];
    int 'array\ index = 1;
    return 'float\ array['array\ index];
}

function passILValuesToFunction() returns [string, int] {
    string 'first\ name = "Bill";
    string 'last\ name = "Kary";
    int age = 40;
    return passILValuesAsParams('first\ name, 'last\ name, age);
}

function passILValuesAsParams(string 'first\ name, string 'last\ name, int 'current\ age) returns [string, int] {
    string 'full\ name = 'first\ name + " " + 'last\ name;
    return ['full\ name, 'current\ age];
}

function testCharInIL() returns (string) {
    string '\\\|\ \!\#\[\{\]\}\ \"\ \u2324 = "sample value";
    return '\\\|\ \!\#\[\{\]\}\ \"\ \u2324;
}

function testFunctionNameWithIL() returns (string) {
    return 'test\ function\ for\ identifier("sample");
}

function 'test\ function\ for\ identifier(string val) returns (string) {
    string s = " test";
    return val + s;
}

public type '\|Test\ Connector\| client object {
    boolean action2Invoked = false;

    public function __init(string param1, string param2, int param3) {

    }

    public remote function action1() returns (string) {
        string '\|sample\ string\| = "this is a sample";
        return '\|sample\ string\|;
    }

    public remote function '\|second\ action\|() returns (string){
       string '\|string\ \|\ value\| = "sample string";
       return '\|string\ \|\ value\|;
    }
};

function testConnectorNameWithIL() returns (string) {
    '\|Test\ Connector\| testConnector = new("MyParam1", "MyParam2", 5);
    string value = testConnector->action1();
    return value;
}

function testConnectorActionWithIL() returns (string) {
    '\|Test\ Connector\| '\|test\ Connector\| = new("MyParam1", "MyParam2", 5);
    string value = '\|test\ Connector\|->'\|second\ action\|();
    return value;
}

function useILInStructName() returns [string, string, int, string?] {
    'family\ person 'person\ one = {'first\ name: "Tom", 'last\ name:"hank", 'current\ age: 50};
    return ['person\ one.'first\ name, 'person\ one.'last\ name, 'person\ one.'current\ age, 'person\ one["first name"]];
}

type 'family\ person record {
    string 'first\ name;
    string 'last\ name;
    int 'current\ age;
};

function testUnicodeInIL() returns (string) {
    string 'සිංහල\ වචනය = "සිංහල වාක්‍යක්";
    return 'සිංහල\ වචනය;
}

function testAcessILWithoutPipe() returns [string, string] {
    string 'x = "hello";
    return ['x, x];
}
 
function testAcessJSONFielAsIL() returns (json|error) {
    json j = {"foo" : {"int" : "I am an integer"}};
    return j.foo.'int;
}

type DOM record {
    string '\{http\:\/\/test\.com\}fname;
};

function testILConsistency() returns DOM {
    DOM d = {'\{http\:\/\/test\.com\}fname: "First Name Element"};
    return d;
}
