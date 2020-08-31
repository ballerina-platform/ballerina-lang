# Documentation for File type
# + path - file path. Example: `C:\users\OddThinking\Documents\My Source\Widget\foo.src`
public class File {
    public string path = "";

    # Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
    # Example:
    # `SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
    # + accessMode - read or write mode
    # + return - success or not
    public function open(string accessMode) returns boolean {
        return true;
    }
}

#
# + anUnion - value of param1
# + anInt - value of param2
# + rest - value of rest param
function insert (string | int | float anUnion, int anInt = 1, string... rest) {

}

#
# + return - `string` value of the X will be returned if found, else an `error` will be returned
function getX () returns string | error {
    return "";
}

#
# + aTuple - a `Tuple` where the values should represent name, age and weight of a person, in order
# + anInt - index
function updatePeople ([string, int, float] aTuple, int anInt) {

}

#
# + return - a `Tuple` will be returned and would represent name, age and weight of a person, in order
function searchPeople () returns [string, int, float] {
    return ["", 1, 1.0];
}

# Test type `typeDef`
# Test service `helloWorld`
# Test variable `testVar`
# Test var `testVar`
# Test function `add`
# Test parameter `x`
# Test const `constant`
# Test annotation `annot`
# + x - one thing to be added
# + y - another thing to be added
# + return - the sum of them
function add (int x, int y) returns int  { return x + y; }
