string output = "";

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i + ":" + v + " ";
}

json j1 = {name:"bob", age:10, pass:true, subjects:[{subject:"maths", marks:75}, {subject:"English", marks:85}]};

function testJSONObject () returns (string) {
    output = "";
    foreach j in j1 {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testJSONArray () returns (string) {
    output = "";
    foreach j in j1.subjects {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testArrayOfJSON () returns string | error {
    output = "";
    json[] array;
    match <json[]> j1.subjects {
        json[] arr1 => array = arr1;
        error err1 => return err1;
    }
    foreach i, j in array {
        concatIntString(i, j.toString() but {error => ""});
    }
    return output;
}

function testJSONString () returns (string) {
    output = "";
    foreach j in j1.name {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testJSONNumber () returns (string) {
    output = "";
    foreach j in j1.age {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testJSONBoolean () returns (string) {
    output = "";
    foreach j in j1.pass {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testJSONNull () returns (string) {
    output = "";
    foreach j in j1.city {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

type Protocols {
    string data;
    Protocol[] plist;
};

type Protocol {
    string name;
    string url;
};

function testJSONToStructCast () returns string | error {
    json j = {data:"data", plist:[{name:"a", url:"h1"}, {name:"b", url:"h2"}]};
    match <Protocols> j {
        Protocols p => {
                output = "";
                foreach protocol in p.plist {
                    concatString(protocol.name + "-" + protocol.url);
                }
                return output;
        }
        error err => return err;
    }
}

function testAddWhileIteration () returns (string) {
    output = "";
    foreach j in j1 {
        if (j.toString() but {() => ""} == "bob") {
            j1["lastname"] = "smith";
        }
    }
    foreach j in j1 {
        concatString(j.toString() but {error => ""});
    }
    return output;
}

function testDeleteWhileIteration () returns (string) {
    output = "";
    foreach j in j1 {
        string str = j.toString() but {() => ""};
        if (str == "bob") {
           any x = j1.remove("subjects");
        }
        concatString(str);
    }

    foreach j in j1 {
        concatString(j.toString() but {error => ""});
    }
    return output;
}
