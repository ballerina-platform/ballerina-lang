# Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
# Example:
# ``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
# + accessMode - read or write mode
# + return - success or not
public function open (string accessMode) returns (boolean) {
    return true;
}

# Represents a Person type in ballerina.
# + name - name of the person.
public type Person object {

    private string name;

    # get the users name.
    # + val - integer value
    function getName(int val) {

    }

    # Indicate whether this is a male or female.
    # + return - True if male
    public function isMale() returns boolean {
        return true;
    }
};
