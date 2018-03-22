string output;

function stringConcat(string key, string value){
    output = output + key + ":" + value + " ";
}

function testMapWithArityOne () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach v in m {
        match <string> v {
            string val1 => val = val1;
            error err => return err;
        }
        stringConcat("_", val);
    }
    return output;
}

function testMapWithArityTwo () returns (string) | error {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach k,v in m {
        match <string> v {
            string val1 => val = val1;
            error err => return err;
        }
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
        match <string> v {
            string val1 => val = val1;
            error err1 => return err1;
        }
        if (k == "a") {
            _ = m.remove("c");
        }
        stringConcat(k, val);
        match <string> m[k] {
            string val2 => mval = val2;
            error err2 => return err2;
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
        match <string> v1 {
            string tempval1 => val1 = tempval1;
            error err1 => return err1;
        }
        stringConcat(k1, val1);
        m[k1 + k1] = val1 + val1;
        foreach k2, v2 in m {
            match <string> v2 {
                string tempval2 => val2 = tempval2;
                error err2 => return err2;
            }
            stringConcat(k2, val2);
        }
        output = output + "\n";
    }
    return output;
}
