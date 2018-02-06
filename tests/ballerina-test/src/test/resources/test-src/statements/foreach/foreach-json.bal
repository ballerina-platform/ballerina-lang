string output = "";

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i + ":" + v + " ";
}

json j1 = {name:"bob", age:10, pass:true, subjects:[{subject:"maths", marks:75}, {subject:"English", marks:85}]};

function testJSONObject () (string) {
    output = "";
    foreach j in j1 {
        concatString(j.toString());
    }
    return output;
}

function testJSONArray () (string) {
    output = "";
    foreach j in j1.subjects {
        concatString(j.toString());
    }
    return output;
}

function testArrayOfJSON () (string) {
    output = "";
    var array, _ = (json[]) j1.subjects;
    foreach i, j in array {
        concatIntString(i, j.toString());
    }
    return output;
}

function testJSONString () (string) {
    output = "";
    foreach j in j1.name {
        concatString(j.toString());
    }
    return output;
}

function testJSONNumber () (string) {
    output = "";
    foreach j in j1.age {
        concatString(j.toString());
    }
    return output;
}

function testJSONBoolean () (string) {
    output = "";
    foreach j in j1.pass {
        concatString(j.toString());
    }
    return output;
}

function testJSONNull () (string) {
    output = "";
    foreach j in j1.city {
        concatString(j.toString());
    }
    return output;
}

struct Protocols {
    string data;
    Protocol[] plist;
}

struct Protocol {
    string name;
    string url;
}

function testJSONToStructCast () (string) {
    json j = {data:"data", plist:[{name:"a", url:"h1"}, {name:"b", url:"h2"}]};
    var protocolsData, _ = <Protocols>j;
    output = "";
    foreach protocol in protocolsData.plist {
        concatString(protocol.name + "-" + protocol.url);
    }
    return output;
}

function testAddWhileIteration () (string) {
    output = "";
    foreach j in j1 {
        if (j.toString() == "bob") {
            j1["lastname"] = "smith";
        }
        concatString(j.toString());
    }
    foreach j in j1 {
        concatString(j.toString());
    }
    return output;
}

function testDeleteWhileIteration () (string) {
    output = "";
    foreach j in j1 {
        if (j.toString() == "bob") {
            j1.remove("subjects");
        }
        concatString(j.toString());
    }
    foreach j in j1 {
        concatString(j.toString());
    }
    return output;
}
