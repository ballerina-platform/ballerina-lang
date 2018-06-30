import ballerina/http;

documentation { Documentation for Test annotation
F{{a}} annotation `field a` documentation
F{{a}} annotation `field a` documentation
F{{b}} annotation `field b` documentation
F{{c}} annotation `field c` documentation}
type Tst record {
    string a;
    string b;
    string cd;
};

annotation Test Tst;

documentation { Documentation for testConst constant
V{{testConst}} abc description}
@final string testConst = "TestConstantDocumentation";

documentation { Documentation for Test struct
F{{a}} struct `field a` documentation
F{{a}} struct `field a` documentation
F{{b}} struct `field b` documentation
F{{c}} struct `field c` documentation}
type Test record {
    int a;
    int b;
    int cdd;
};

documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
P{{accessMode}} read or write mode
P{{accessMode}} read or write mode
R{{successful}} boolean `true` or `false`
}
public function File::open (string accessMode) returns (boolean) {
    boolean successful;
    return successful;
}

documentation { Documentation for File object
F{{path}} file path. Example: ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
}
public type File object {

    public string path;


    public function open(string accessMode) returns boolean;
};

//documentation {
// Transformer Foo Person -> Employee
// T{{pa}} input struct Person source used for transformation
// T{{e}} output struct Employee struct which Person transformed to
// T{{e}} output struct Employee struct which Person transformed to
// P{{defaultAddress}} address which serves Eg: `POSTCODE 112`
//}
//transformer <Person p, Employee e> Foo(any defaultAddress) {
//    e.name = p.firstName;
//}

type Person record {
    string firstName;
    string lastName;
    int age;
    string city;
};

type Employee record {
    string name;
    int age;
    string address;
    any ageAny;
};

documentation {
Test Connector
F{{url}} url for endpoint
F{{url}} url for endpoint
P{{urls}} urls for endpoint}
type TestConnector record {
  string url;
};

//documentation {Test Connector action testAction
//    R{{s}} which represent successful or not
//    R{{ssss}} which represent successful or not}
//public function <TestConnector t> testAction(string url) returns (boolean) {
//    boolean value;
//return value;
//}

documentation { PizzaService HTTP Service
P{{conn}} HTTP connection.}
@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> PizzaService {

    @http:ResourceConfig {
        path:"/"
    }
    documentation {
    Check orderPizza resource.
    P{{req}} In request.
    P{{req}} In request.
    P{{reqest}} In request.}
//  P{{conn}} HTTP connection. Commented due to https://github.com/ballerina-lang/ballerina/issues/5586 issue
    orderPizza(endpoint conn, http:Request req) {
            http:Response res = new;
            _ = conn -> respond(res);
        }
    }

documentation { Documentation for testConst constant
V{{testConstd}} abc description}
@final string testConsts = "TestConstantDocumentation";
