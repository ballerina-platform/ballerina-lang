import ballerina/http;

# Documentation for Test annotation
# function `9invalidFunc`
# function `invalidFunc`
# parameter `invalidParameter`
# + a - `field a` documentation
# + a - `field a` documentation
# + b - `field b` documentation
# + c - `field c` documentation
# + return - description
type Tst record {
    string a;
    string b;
    string cd;
};

annotation Test Tst;

# Documentation for testConst constant
# service `9invalidServ`
# service `invalidServ`
# parameter `invalidParameter`
final string testConst = "TestConstantDocumentation";

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

# Documentation for File object
# + path - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
# + path - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
# + path2 - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
# const `9invalidConst`
# const `invalidConst`
# parameter `invalidParameter`
public class File {
    public string path = "";
    public string path3 = "";

    # Gets a access parameter value (`true` or `false`) for a given key. Please note that `foo` will always be bigger than `bar`.
    # Example:
    # `SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
    # + accessMode - read or write mode
    # + accessMode - read or write mode
    # + successful - boolean `true` or `false`
    public function open (string accessMode) returns (boolean) {
        boolean successful = false;
        return successful;
    }
}

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
# const `9invalidConst`
# const `invalidConst`
# parameter `invalidParameter`
# + conn - HTTP connection.
# + return - description
@http:ServiceConfig {
    basePath:"/hello"
}
service PizzaService on new http:MockListener(9090) {

    # Check orderPizza resource.
    # + req - In request.
    # + req - In request.
    # + reqest - In request.
    @http:ResourceConfig {
        path:"/"
    }
    resource function orderPizza(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }
}

# Documentation for testConst constant
# + testConstd - abc description
# + return - description
final string testConsts = "TestConstantDocumentation";


# Documentation for load function.
# function `9function`
# parameter `filePath1`
public function load(string filePath) {

}
