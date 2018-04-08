documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
T{{file}} file path ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
P{{accessMode}} read or write mode
}
public function <File file> open (string accessMode) returns (boolean) {
    return true;
}

documentation { Documentation for File type
F{{path}} type `field path` documentation
}
public type File {
    string path;
};
