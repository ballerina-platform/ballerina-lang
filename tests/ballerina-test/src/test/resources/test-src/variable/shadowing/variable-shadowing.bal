string name;

function printName(any name) {
    var name = formatName(name);
    if (name == ""){
        var getName = () => (string) {
            string name = "Ballerina";
            return "";
        };
        name = getName();
    }
}

function formatName(any a) returns any {
    return a;
}

type User object {
    public {
        string name;
    }
};
