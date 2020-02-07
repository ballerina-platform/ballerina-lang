string output = "";

function stringConcat(string value){
    output = output + value + " ";
}

function testMapWithArityOne () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var v in m {
        val = <string> v;
        stringConcat(val);
    }
    return output;
}

function testMapWithArityTwo () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var v in m {
        val = <string> v;
        stringConcat(val);
    }
    return output;
}

function testDeleteWhileIteration () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val;
    foreach var v1 in m.keys() {
        if (v1 == "a") {
            _ = m.remove("c");
            foreach var v2 in m {
                val = <string> v2;
                stringConcat(val);
            }
        }
    }
    return output;
}

function testAddWhileIteration () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val1;
    string val2;
    foreach var v1 in m {
		val1 = <string> v1;
        stringConcat(val1);
        m[val1 + val1] = val1 + val1;
        foreach var v2 in m {
            val2 = <string> v2;
            stringConcat(val2);
        }
        output = output + "\n";
    }
    return output;
}
