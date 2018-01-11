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
