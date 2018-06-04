documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
P{{accessMode}} read or write mode
R{{}} success or not
}
public function File::open (string accessMode) returns (boolean) {
    return true;
}

documentation { Documentation for File type
F{{path}} file path. Example: ``C:\users\OddThinking\Documents\My Source\Widget\foo.src``
}
public type File object {
    public {
        string path;
    }

    public function open(string accessMode) returns boolean;
};

documentation {
P{{anUnion}} value of param1
P{{anInt}} value of param1
}
function insert (string | int | float anUnion, int anInt) {

}

documentation {
R{{}} `string` value of the X will be returned if found, else an `error` will be returned
}
function getX () returns string | error {
  return "";
}

documentation {
    P{{aTuple}} a `Tuple` where the values should represent name, age and weight of a person, in order
    P{{anInt}} index
}
function updatePeople ((string, int, float) aTuple, int anInt) {

}

documentation {
    R{{}} a `Tuple` will be returned and would represent name, age and weight of a person, in order
}
function searchPeople () returns ((string, int, float)) {
    return ("", 1, 1.0);
}