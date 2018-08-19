import ballerina/io;

public function noParamEntry() returns int {
    return 1;
}

public function publicNonMainEntry(int i, string s) {
    io:println(i, " ", s);
}

function nonPublicEntry(int i) {
    io:println(i);
}

public function jsonEntry(json j) returns json {
    return j;
}

public function arrayUnionEntry(int[]|float[]|boolean[]|json[] arr) returns any {
    return arr;
}

public function typedescEntry(typedesc t) returns typedesc {
    return t;
}

public function defaultableParamEntry(int i = 1, boolean b, string s = "default hello", string s2) returns string {
    return <string> i + " " + s + " world: " + s2 + " " + <string> b;
}

public function combinedTypeEntry(int i, float f, string s, byte b, boolean bool, json j, xml x, Employee e,
                                  string... args) returns string {
    string restArgs;
    foreach str in args {
        restArgs += str + " ";
    }
    // todo: <string> for float and remove <int> for byte
    return "integer: " + <string> i + ", float: " + f + ", string: " + s + ", byte: " + <string> <int> b
                + ", boolean: " + <string> bool + ", JSON Name Field: " + j.name.toString() + ", XML Element Name: "
                    + x.getElementName() + ", Employee Name Field: " + e.name + ", string rest args: " + restArgs;
}

public function oneDimensionalArrayEntry(int[] intArr, string[] strArr, float[] floatArr, boolean[] boolArr,
json[] jsonArr, Employee[] empArr) returns string {
    return "integer: " + intArr[1] + ", string: " + strArr[1] + ", float: " + floatArr[1]  + ", boolean: "
        + <string> boolArr[1] + ", json: " + jsonArr[1].toString()
        + ", Employee Name Field: " + empArr[1].name;
}

public function mapEntry(map<int> intMap, map<string> strMap, map<float> floatMap, map<boolean> boolMap,
                            map<json> jsonMap, map<Employee> empMap) returns string {
    return "integer: " + intMap.test + ", string: " + strMap.test + ", float: " + floatMap.test  + ", boolean: "
            + <string> boolMap.test + ", json: " + jsonMap.test.toString() + ", Test Employee Name Field: "
            + empMap.test.name;
}

public type Employee record {
   string name;
};
