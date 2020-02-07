string output = "";

type Employee record {
    int id;
    string name;
    float salary;
};

table<Employee> data = table {
    { key id, name, salary },
    [
        { 1, "Mary",  300.5 },
        { 2, "John",  200.5 },
        { 3, "Jim", 330.5 }
    ]
};

function concatIntAny(int i, any a) {
    output = output + i.toString() + ":" + a.toString() + " ";
}

function concatIntIntStringFloat(int i1, int i2, string s, float f) {
    output = output + i1.toString() + ":" + i2.toString() + ":" + s + ":" + f.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testTableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var v in data {
        concatIntAny(i, v);
        i += 1;
    }
    return output;
}

function testTableWithType() returns string {
    output = "";

    int i = 0;
    foreach Employee v in data {
        concatIntAny(i, v);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testRecordInTableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var {id, name, salary} in data {
    concatIntIntStringFloat(i, id, name, salary);
        i += 1;
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyTableIteration() returns string {
    output = "";

    table<Employee> d = table {
        { key id, name, salary }, []
    };

    int i = 0;
    foreach var {id, name, salary} in d {
    concatIntIntStringFloat(i, id, name, salary);
        i += 1;
    }
    return output;
}
