string output;

function stringConcat(string key, string value){
    output = output + key + ":" + value + " ";
}

function testMapWithArityOne ()(string) {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    foreach v in m {
        var val,_ = (string) v;
        stringConcat("_", val);
    }
    return output;
}

function testMapWithArityTwo ()(string) {
    map m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    foreach k,v in m {
        var val,_ = (string) v;
        stringConcat(k, val);
    }
    return output;
}

function testDeleteWhileIteration () (string) {
    map m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    foreach k, v in m {
        var val, _ = (string)v;
        if (k == "a") {
            _ = m.remove("c");
        }
        stringConcat(k, val);
        var mval, _ = (string)m[k];
        stringConcat(k, mval);
    }
    return output;
}

function testAddWhileIteration () (string) {
    map m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    foreach k1, v1 in m {
        var val1, _ = (string)v1;
        stringConcat(k1, val1);
        m[k1 + k1] = val1 + val1;
        foreach k2, v2 in m {
            var val2, _ = (string)v2;
            stringConcat(k2, val2);
        }
        output = output + "\n";
    }
    return output;
}
