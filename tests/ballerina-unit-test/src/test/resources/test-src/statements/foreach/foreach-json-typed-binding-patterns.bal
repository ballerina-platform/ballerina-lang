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
    output = output + i + ":" + s + " ";
}

function concatIntJson(int i, json j) {
    output = output + i + ":" + j.toString() + " ";
}

function concatIntStringAny(int i, string s, anydata a) {
    output = output + i + ":" + s + ":" + string.convert(a) + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testJsonWithoutType() returns string|error {
    output = "";

    int i = 0;
    foreach var (k, v) in check map<json>.convert(jdata) {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testJsonWithType() returns string|error {
    output = "";

    int i = 0;
    foreach (string, json) (k, v) in check map<json>.convert(jdata) {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testDirectAccessJsonArrayWithoutType() returns string {
    output = "";

    int i = 0;
    json j = jdata["subjects"];
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
    json j = jdata["subjects"];
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

    json subjects = jdata["subjects"];

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

    json subjects = jdata["subjects"];

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

    json j = jdata["random"];

    int i = 0;
    foreach var (k, v) in check map<json>.convert(j) {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

function testDirectAccessInvalidElementWithType() returns string|error {
    output = "";

    json j = jdata["random"];

    int i = 0;
    foreach (string, json) (k, v) in check map<json>.convert(j) {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testIteratingCompleteJsonWithoutType() returns string|error {
    output = "";

    int i = 0;
    foreach var (k, v) in check map<json>.convert(jdata) {
        if v is json[] {
            foreach var w in v {
                concatIntStringAny(i, k, w);
            }
        } else {
            concatIntStringAny(i, k, v);
        }
        i += 1;
    }
    return output;
}

function testIteratingCompleteJsonWithType() returns string|error {
    output = "";

    int i = 0;
    foreach (string, json) (k, v) in check map<json>.convert(jdata) {
        if v is json[] {
            foreach json w in v {
                concatIntStringAny(i, k, w);
            }
        } else {
            concatIntStringAny(i, k, v);
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
    foreach var (k, v) in check map<json>.convert(j) {
        concatIntStringAny(i, k, v);
        i += 1;
    }
    return output;
}
