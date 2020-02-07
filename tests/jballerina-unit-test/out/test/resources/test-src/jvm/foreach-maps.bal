string output = "";

function stringConcat(string key, string value){
    output = output + key + ":" + value + " ";
}

function testMapWithArityOne () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var [k, v] in m.entries() {
        val = <string> v;
        stringConcat("_", val);
    }
    return output;
}

function testMapWithArityTwo () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var [k, v] in m.entries() {
        val = <string> v;
        stringConcat(k, val);
    }
    return output;
}

function testAddWhileIteration () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val1;
    string val2;
    foreach var [k1, v1] in m.entries() {
		val1 = <string> v1;
        stringConcat(k1, val1);
        m[k1 + k1] = val1 + val1;
        foreach var [k2, v2] in m.entries() {
            val2 = <string> v2;
            stringConcat(k2, val2);
        }
        output = output + "\n";
    }
    return output;
}
