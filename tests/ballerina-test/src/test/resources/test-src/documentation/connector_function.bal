documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
P{{accessMode}} read or write mode
}
public function <File file> open (string accessMode) returns (boolean) {
    boolean successful = false;
    return successful;
}

documentation { Documentation for File struct
F{{path}} struct `field path` documentation
}
public struct File {
    string path;
}

documentation {Test Connector
F{{url}} url for endpoint
F{{path}} path for endpoint
}
struct TestConnector {
    string url;
    string path;
}

documentation {Test Connector action testAction}
function <TestConnector t> testAction () returns (boolean) {
    boolean value;
    return value;
}

documentation {Test Connector action testSend
P{{ep}} which represent successful or not
}
function <TestConnector t> testSend (string ep) returns (boolean) {
    boolean value;
    return value;
}

