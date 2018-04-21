string output;

function stringConcat(string key, string value){
    output = output + key + ":" + value + " ";
}

function testMapWithArityOne () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach v in m {
        val = <string> v;
        stringConcat("_", val);
    }
    return output;
}

function testMapWithArityTwo () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach k,v in m {
        val = <string> v;
        stringConcat(k, val);
    }
    return output;
}

function testDeleteWhileIteration () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val;
    string mval;
    foreach k, v in m {
        val = <string> v;
        if (k == "a") {
            _ = m.remove("c");
        }
        stringConcat(k, val);
        if (m.hasKey(k)){
            mval = <string>m[k];
        } else {
            mval = "null";
        }
        stringConcat(k, mval);
    }
    return output;
}

function testAddWhileIteration () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val1;
    string val2;
    foreach k1, v1 in m {
		val1 = <string> v1;
        stringConcat(k1, val1);
        m[k1 + k1] = val1 + val1;
        foreach k2, v2 in m {
            val2 = <string> v2;
            stringConcat(k2, val2);
        }
        output = output + "\n";
    }
    return output;
}
