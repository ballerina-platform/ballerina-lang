string output = "";

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i.toString() + ":" + v + " ";
}

function testJSONObject () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in  <map<json>>j1.cloneReadOnly() {
        concatString(j.toJsonString());
    }
    return output;
}

function testJSONArray () returns (string) {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    json element = <json> j1.subjects;
    if element is json[] {
        foreach var j in element {
            concatString(j.toJsonString());
        }
    }
    return output;
}

function testArrayOfJSON () returns string | error {
    output = "";
    json[] array = [];
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    var arr1 = j1.subjects;
    if arr1 is json[] {
        array = arr1;
    }

    int i = 0;
    foreach var j in array {
        concatIntString(i, j.toJsonString());
        i += 1;
    }
    return output;
}

function testJSONString () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1.name {
        concatString(j.toJsonString());
    }
    return output;
}

function testJSONNumber () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1.age {
        concatString(j.toJsonString());
    }
    return output;
}

function testJSONBoolean () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1.pass {
        concatString(j.toJsonString());
    }
    return output;
}

function testJSONNull () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1.city {
        concatString(j.toJsonString());
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

//TODO: enable when 'json -> record' is supported
//function testJSONToStructCast () returns string|error {
//    json j = {data:"data", plist:[{name:"a", url:"h1"}, {name:"b", url:"h2"}]};
//    var result = check <Protocols>j;
//    output = "";
//    foreach var protocol in result.plist {
//        concatString(protocol.name + "-" + protocol.url);
//    }
//    return output;
//}

function testAddWhileIteration () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1 {
        if (j.toString() == "bob") {
            map<json> temp = <map<json>>j1;
            temp["lastname"] = "smith";
            j1 = <json>temp;
        }
    }
    foreach var j in <map<json>>j1 {
        concatString(j.toJsonString());
    }
    return output;
}

function testDeleteWhileIteration () returns string|error {
    output = "";
    json j1 = {name:"bob", age:10, pass:true, subjects: [{subject:"maths", marks:75}, {subject:"English", marks:85}]};
    foreach var j in <map<json>>j1 {
        string str = j.toJsonString();
        if (str == "bob") {
            map<json> temp = (<map<json>>j1);
            any x = temp.remove("subjects");
        }
        concatString(str);
    }

    foreach var j in <map<json>>j1 {
        concatString(j.toJsonString());
    }
    return output;
}
