import ballerina/io;

public function noargentry() returns int {
    return 1;
}

public function nomoremain(int i, string s) {
    io:println(i, " ", s);
}

function nonpublicnonmain(int i) {
    io:println(i);
}

public function jsonentry(json j) returns json {
    return j;
}

public function stringarrayentry(string[] arr) returns string[] {
    return arr;
}

public function unionarrayentry(int[]|float[]|boolean[]|json[] arr) returns any {
    return arr;
}

public function defaultparamentry(int i = 1, boolean b, string s = "default hello", string s2) returns string {
    return <string> i + " " + s + " world: " + s2 + " " + <string> b;
}

public function combinedmain(int i, float f, string s, byte b, boolean bool, json j, xml x, Employee e, string... args)
                returns string {
    string restArgs;
    foreach str in args {
        restArgs += str + " ";
    }
    return "integer: " + <string> i + ", float: " + f + ", string: " + s + ", byte: " + <string> <int> b
                + ", boolean: " + <string> bool + ", JSON Name Field: " + j.name.toString() + ", XML Element Name: "
                    + x.getElementName() + ", Employee Name Field: " + e.name + ", string rest args: " + restArgs;
}

public type Employee record {
   string name;
};
