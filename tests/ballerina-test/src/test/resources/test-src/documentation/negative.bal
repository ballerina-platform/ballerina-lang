import ballerina.net.http;

documentation { Documentation for Test annotation
F{{a}} annotation `field a` documentation
F{{a}} annotation `field a` documentation
F{{b}} annotation `field b` documentation
F{{c}} annotation `field c` documentation}
annotation Test {
    string a;
    string b;
    string cd;
}

documentation { Documentation for testConst constant
V{{testConst}} abc description}
const string testConst = "TestConstantDocumentation";

documentation { Documentation for state enum
F{{foo}} enum `field foo` documentation
F{{foo}} enum `field foo` documentation
F{{bar}} enum `field bar` documentation}
enum state {
    foo,
    bars
}

documentation { Documentation for Test struct
F{{a}} struct `field a` documentation
F{{a}} struct `field a` documentation
F{{b}} struct `field b` documentation
F{{c}} struct `field c` documentation}
struct Test {
    int a;
    int b;
    int cdd;
}

documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
P{{accessMode}} read or write mode
R{{successfuls}} boolean `true` or `false`
}
public function <File file> open (string accessMode) (boolean successful) {
    return successful;
}

documentation { Documentation for File struct
F{{path}} struct `field path` documentation
}
public struct File {
    string path;
}

documentation {
 Transformer Foo Person -> Employee
 T{{pa}} input struct Person source used for transformation
 T{{e}} output struct Employee struct which Person transformed to
 T{{e}} output struct Employee struct which Person transformed to
 P{{defaultAddress}} address which serves Eg: `POSTCODE 112`
}
transformer <Person p, Employee e> Foo(any defaultAddress) {
    e.name = p.firstName;
}

struct Person {
    string firstName;
    string lastName;
    int age;
    string city;
}

struct Employee {
    string name;
    int age;
    string address;
    any ageAny;
}

documentation {
Test Connector
P{{url}} url for endpoint
P{{url}} url for endpoint
P{{urls}} urls for endpoint}
connector TestConnector (string url) {

    documentation {Test Connector action testAction
    R{{s}} which represent successful or not
    R{{s}} which represent successful or not
    R{{ssss}} which represent successful or not}
    action testAction() (boolean s) {
       boolean value;
       return value;
    }
}

documentation { PizzaService HTTP Service
P{{conn}} HTTP connection.}
service<http> PizzaService {

    documentation {
    Check orderPizza resource.
    P{{conn}} HTTP connection.
    P{{req}} In request.
    P{{req}} In request.
    P{{reqest}} In request.}
    resource orderPizza(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }
}

documentation { Documentation for testConst constant
V{{testConstd}} abc description}
const string testConsts = "TestConstantDocumentation";
