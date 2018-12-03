string output = "";

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i + ":" + v + " ";
}

json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};

function testJSONObject () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1) {
        concatString(j.toString());
    }
    return output;
}

function testJSONArray () returns (string) {
    output = "";
    json element = j1.subjects;
    if element is json[] {
        foreach var j in element {
            concatString(j.toString());
        }
    }
    return output;
}

function testArrayOfJSON () returns string | error {
    output = "";
    json[] array = [];
    var arr1 = j1.subjects;
    if arr1 is json[] {
        array = arr1;
    }

    int i = 0;
    foreach var j in array {
        concatIntString(i, j.toString());
        i += 1;
    }
    return output;
}

function testJSONString () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1.name) {
        concatString(j.toString());
    }
    return output;
}

function testJSONNumber () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1.age) {
        concatString(j.toString());
    }
    return output;
}

function testJSONBoolean () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1.pass) {
        concatString(j.toString());
    }
    return output;
}

function testJSONNull () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1.city) {
        concatString(j.toString());
    }
    return output;
}

type Protocols record {
    string data;
    Protocol[] plist;
};

type Protocol record {
    string name;
    string url;
};

function testJSONToStructCast () returns string|error {
    json j = {data:"data", plist:[{name:"a", url:"h1"}, {name:"b", url:"h2"}]};
    var result = check Protocols.convert(j);
    output = "";
    foreach var protocol in result.plist {
        concatString(protocol.name + "-" + protocol.url);
    }
    return output;
}

function testAddWhileIteration () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1) {
        if (j.toString() == "bob") {
            j1["lastname"] = "smith";
        }
    }
    foreach var (i, j) in check map<json>.convert(j1) {
        concatString(j.toString());
    }
    return output;
}

function testDeleteWhileIteration () returns string|error {
    output = "";
    foreach var (i, j) in check map<json>.convert(j1) {
        string str = j.toString();
        if (str == "bob") {
           any x = j1.remove("subjects");
        }
        concatString(str);
    }

    foreach var (i, j) in check map<json>.convert(j1) {
        concatString(j.toString());
    }
    return output;
}
