import ballerina/http;

documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
P{{accessMode}} read or write mode
R{{successful}} boolean `true` or `false`}
deprecated {
  This function is deprecated use `openFile(string accessMode){}` instead.
}
public function <File file> open (string accessMode) (boolean successful) {
    return successful;
}

documentation { Documentation for File struct
F{{path}} struct `field path` documentation}
deprecated {
  This Struct is deprecated use `File2` instead.
}
public struct File {
    string path;
}

documentation { Documentation for state enum
F{{foo}} enum `field foo` documentation
F{{bar}} enum `field bar` documentation}
deprecated {
  This Enum is deprecated use `Enum2` instead.
}
enum state {
    foo,
    bar
}

documentation { PizzaService HTTP Service }
deprecated {
  This Service is deprecated use `PizzaHutService{}` instead.
}
service<http> PizzaService {

    deprecated {This Resource is deprecated use `PizzaHutService.orderFromPizza()` instead.}
    resource orderPizza(http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.respond(res);
    }

}

documentation {Documentation for Test annotation
F{{a}} annotation `field a` documentation
F{{b}} annotation `field b` documentation
F{{c}} annotation `field c` documentation}
deprecated {
  This annotation is deprecated use `annotationTest{ string a; string b; string c; }` instead.
}
annotation Test {
    string a;
    string b;
    string c;
}

documentation {Test Connector
P{{url}} url for endpoint}
deprecated {
  This Connector is deprecated use `Connector(string url2){}` instead.
}
connector TestConnector (string url) {

    deprecated {
      This action is deprecated use `Connector.test(string url2){}` instead.
    }
    action testAction() (boolean s) {
       boolean value;
       return value;
    }

}

documentation { Documentation for testConst constant
V{{testConst}} constant variable `testConst`}
deprecated {use ```const string testConst = "TestConstantDocumentation";``` instead}
@final string testConst = "TestConstantDocumentation";
