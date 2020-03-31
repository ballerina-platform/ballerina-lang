import ballerina/crypto;

# A Lorry
public type Lorry object {
    # Get colour
    # + a - input
    # + return - a string
    public function get_color(int a) returns Color{
        return new Color();
    }
};

# A Car
public type Color object {
    # Print colour
    # + b - input
    # + return - a string
    public function print(string b) returns string{
        return "red";
    }
};

public function foo(string... args) {
    Lorry lorry = new Lorry();
    lorry.get_color(2).print("").toString().length();
    lorry.get_color(2).print(".invoc(\"");
}

function getCacheId(string str) {
    crypto:hashMd5(str.toBytes());
}
