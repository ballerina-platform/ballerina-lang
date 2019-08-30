string output = "";

json jdata = {
    name: "bob",
    age: 10,
    pass: true,
    subjects: [
        { subject: "maths", marks: 75 },
        { subject: "English", marks: 85 }
    ]
};

function concatIntString(int i, string s) {
    output = output + i.toString() + ":" + s + " ";
}

function concatIntJson(int i, json j) {
    output = output + i.toString() + ":" + j.toJsonString() + " ";
}

function concatIntStringAny(int i, anydata a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testJsonWithoutType() returns string|error {
    output = "";

    int i = 0;
    foreach var v in <map<json>>jdata.cloneReadOnly() {
        concatIntStringAny(i, v.toJsonString());
        i += 1;
    }
    return output;
}

function testJsonWithType() returns string|error {
    output = "";

    int i = 0;
    foreach json v in <map<json>>jdata.cloneReadOnly() {
        concatIntStringAny(i, v.toJsonString());
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testDirectAccessJsonArrayWithoutType() returns string {
    output = "";

    int i = 0;
    json j = <json>jdata.subjects;
    if j is json[] {
        foreach var v in j {
            concatIntJson(i, v);
            i += 1;
        }
    }
    return output;
}

function testDirectAccessJsonArrayWithType() returns string {
    output = "";

    int i = 0;
    json j =  <json>jdata.subjects;
    if j is json[] {
        foreach json v in j {
            concatIntJson(i, v);
            i += 1;
        }
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testJsonArrayWithoutType() returns string {
    output = "";

    json subjects =  <json>jdata.subjects;

    int i = 0;
    if subjects is json[] {
        foreach var v in subjects {
            concatIntJson(i, v);
            i += 1;
        }
    }
    return output;
}

function testJsonArrayWithType() returns string {
    output = "";

    json subjects =  <json>jdata.subjects;

    int i = 0;
    if subjects is json[] {
        foreach json v in subjects {
            concatIntJson(i, v);
            i += 1;
        }
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testDirectAccessInvalidElementWithoutType() returns string|error {
    output = "";

    json j =  <json>jdata.random;

    int i = 0;
    foreach var v in <map<json>>j {
        concatIntStringAny(i, v.toJsonString());
        i += 1;
    }
    return output;
}

function testDirectAccessInvalidElementWithType() returns string|error {
    output = "";

    json j =  <json>jdata.random;

    int i = 0;
    foreach json v in <map<json>>j {
        concatIntStringAny(i, v.toJsonString());
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testIteratingCompleteJsonWithoutType() returns string|error {
    output = "";

    int i = 0;
    foreach var v in <map<json>>jdata.cloneReadOnly() {
        if v is json[] {
            foreach var w in v {
                concatIntStringAny(i, w.toJsonString());
            }
        } else {
            concatIntStringAny(i, v.toJsonString());
        }
        i += 1;
    }
    return output;
}

function testIteratingCompleteJsonWithType() returns string|error {
    output = "";

    int i = 0;
    foreach json v in <map<json>>jdata.cloneReadOnly() {
        if v is json[] {
            foreach json w in v {
                concatIntStringAny(i, w.toJsonString());
            }
        } else {
            concatIntStringAny(i, v.toJsonString());
        }
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyJsonIteration() returns string|error {
    output = "";

    json j = {};

    int i = 0;
    foreach var v in <map<json>>j.cloneReadOnly() {
        concatIntStringAny(i, v.toJsonString());
        i += 1;
    }
    return output;
}
