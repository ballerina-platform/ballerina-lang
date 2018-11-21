import ballerina/http;

# Documentation for Test annotation
# + a - annotation `field a` documentation
# + a - annotation `field a` documentation
# + b - annotation `field b` documentation
# + c - annotation `field c` documentation
# + return - description
type Tst record {
    string a;
    string b;
    string cd;
};

annotation Test Tst;

# Documentation for testConst constant
@final string testConst = "TestConstantDocumentation";

# Documentation for Test struct
# + a - struct `field a` documentation
# + a - struct `field a` documentation
# + b - struct `field b` documentation
# + c - struct `field c` documentation
type Test record {
    int a;
    int b;
    int cdd;
};

# Gets a access parameter value (`true` or `false`) for a given key. Please note that `foo` will always be bigger than `bar`.
# Example:
# `SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
# + accessMode - read or write mode
# + accessMode - read or write mode
# + successful - boolean `true` or `false`
function File.open (string accessMode) returns (boolean) {
    boolean successful = false;
    return successful;
}

# Documentation for File object
# + path - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
# + path - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
# + path2 - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
public type File object {
    public string path = "";
    public string path3 = "";

    public function open(string accessMode) returns boolean;
};

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

# Test Connector
# + url - url for endpoint
# + url - url for endpoint
# + urls - urls for endpoint
type TestConnector record {
  string url;
  string url2;
};


# PizzaService HTTP Service
# + conn - HTTP connection.
# + return - description
@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> PizzaService {

    # Check orderPizza resource.
    # + req - In request.
    # + req - In request.
    # + reqest - In request.
    @http:ResourceConfig {
        path:"/"
    }
    orderPizza(endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }
}

# Documentation for testConst constant
# + testConstd - abc description
# + return - description
@final string testConsts = "TestConstantDocumentation";
