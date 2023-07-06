import ballerina/lang.test;

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
service /PizzaService on new test:MockListener(9090) {

    # Check orderPizza resource.
    # + req - In request.
    # + req - In request.
    # + reqest - In request.
    resource function get orderPizza(string conn, string req) {

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

# + a - parameter a
# + return - `float` return parameter is float
# # parameter `invalidParameter`
[int, float, string] [a, b, c] = [1, 2.5, "Mac"];

# + a - parameter a
# + return - `float` return parameter is float
# parameter `invalidParameter`
record {int a;} {a:myA} = {a:5};

# + message - parameter message
# + return - `float` return parameter is float
# parameter `invalidParameter`
error error(message) = error ("stack over flow");

# Description
#
# + name1 - My Name
public function myFunc1(string name1, string val1) {

}

# Description
#
# + name2 - My Name
function myFunc2(string name2, string val2) {

}

# Description
# + v\	- Parameter
# + a\
public function main1() {
}
