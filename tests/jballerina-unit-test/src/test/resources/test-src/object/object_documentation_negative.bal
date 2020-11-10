import ballerina/http;

# Documentation for Test annotation
#
# + a - `field a` documentation
# + a - `field a` documentation
# + b - `field b` documentation
# + c - `field c` documentation
type Tst record {
    string a = "";
    string b = "";
    string cd = "";
};

annotation Tst Test;

# Documentation for testConst constant
#
# + testConst - abc description
final string testConst = "TestConstantDocumentation";

# Documentation for Test struct
#
# + a - struct `field a` documentation
# + a - struct `field a` documentation
# + b - struct `field b` documentation
# + c - struct `field c` documentation
class Test {
    public int a = 0;
    public int b = 0;
    public int cdd = 0;
}

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
    public function open(string accessMode) returns boolean {
        boolean successful = false;
        return successful;
    }
}

class Person {
    public string firstName = "";
    public string lastName = "";
    public int age = 0;
    public string city = "";
}

class Employee {
    public string name = "";
    public int age = 0;
    public string address = "";
    public any ageAny = ();
}

# Test Connector
#
# + url - url for endpoint
# + url - url for endpoint
# + urls - urls for endpoint
class TestConnector {
  public string url = "";
}

# PizzaService HTTP Service
#
# + conn - HTTP connection.
service PizzaService on new http:Listener(9997) {

    # Check orderPizza resource.
    #
    # + req - In request.
    # + req - In request.
    # + reqest - In request.
//  # + conn - HTTP connection. Commented due to https://github.com/ballerina-lang/ballerina/issues/5586 issue

    resource function orderPizza(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }
}

# Documentation for testConst constant
#
# + testConstd - abc description
final string testConsts = "TestConstantDocumentation";

# Docs for this record.
# + abc - the field documented at object level. Known type `Bar`, unknown type `Baz`.
public class Foo {

    # The field re-documented at field level.
    # Known type `Bar`, unknown type `Baz`.
    public string abc = "str";

    // The undocumented field.
    public int def = 1;

    # The field documented at field level.
    # Known type `Bar`, unknown type `Baz`.
    string ghi = "str2";
}

type Bar record {
};
