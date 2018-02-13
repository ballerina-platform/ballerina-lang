documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
`SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
- #file file path `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
- #accessMode read or write mode
- #successful boolean `true` or `false`
}
public function <File file> open (string accessMode) (boolean successful) {
    return successful;
}

documentation { Documentation for File struct
- #path struct `field path` documentation
}
public struct File {
    string path;
}