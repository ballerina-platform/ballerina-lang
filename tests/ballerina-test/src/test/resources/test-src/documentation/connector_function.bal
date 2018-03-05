documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
P{{accessMode}} read or write mode
R{{successful}} boolean `true` or `false`
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

documentation {Test Connector
P{{url}} url for endpoint
P{{path}} path for endpoint
}
connector TestConnector (string url, string path) {

    documentation {Test Connector action testAction R{{s}} which represent successful or not}
    action testAction() (boolean s) {
       boolean value;
       return value;
    }

    documentation {Test Connector action testSend P{{ep}} which represent successful or not R{{s}} which represent successful or not}
    action testSend(string ep) (boolean s) {
        boolean value;
        return value;
    }
}

