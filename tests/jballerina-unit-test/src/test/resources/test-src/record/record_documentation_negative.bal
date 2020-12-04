import ballerina/lang.test;

# Documentation for Test annotation
#
# + a - `field a` documentation
# + a - `field a` documentation
# + b - `field b` documentation
# + c - `field c` documentation
type Tst record {
    string a;
    string b;
    string cd;
};

annotation Test Tst;

# Documentation for testConst constant
#
# + testConstd - abc description
final string testConsts = "TestConstantDocumentation";

# Documentation for Test struct
#
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
#
# + path - file path. Example: ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
public class File {

    public string path = "";

    # Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
    # Example:
    # ``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
    #
    # + accessMode - read or write mode
    # + accessMode - read or write mode
    # + successfuls - boolean `true` or `false`
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
#
# + url - url for endpoint
# + url - url for endpoint
# + urls - urls for endpoint
type TestConnector record {
  string url;
};

# PizzaService
#
# + conn - connection.
service /PizzaService on new test:MockListener (9090) {

    # Check orderPizza resource.
    #
    # + req - In request.
    # + req - In request.
    # + reqest - In request.
//  # + conn - connection. Commented due to https://github.com/ballerina-lang/ballerina/issues/5586 issue

    resource function get orderPizza(string conn, string req) {

    }
}

# Docs for this record.
# + abc - the field documented at record level. Known type `Bar`, unknown type `Baz`.
public type Foo record {

    # The field re-documented at field level.
    # Known type `Bar`, unknown type `Baz`.
    string abc;

    // The undocumented field.
    int def;

    # The field documented at field level.
    # Known type `Bar`, unknown type `Baz`.
    string ghi;
};

type Bar record {
};

