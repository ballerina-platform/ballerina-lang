documentation {
Gets a access parameter value (`true` or `false`) for a given key. Please note that #foo will always be bigger than #bar.
Example:
``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``
P{{accessMode}} read or write mode
R{{}} success or not
}
public function open (string accessMode) returns (boolean) {
    return true;
}

documentation {
    Represents a Person type in ballerina.
}
public type Person object {
    documentation {
        This is the name of the person.
    }
    private string name;

    documentation {
        get the users name.
        P{{val}} integer value
    }
    function getName(int val) {

    }

    documentation {
        Indecate whether this is a male or female.
        R{{}} True if male
    }
    public function isMale() returns boolean;
};

documentation {
    male implementation

    R{{}} true allways
}
function Person::isMale() returns boolean {
    return true;
}