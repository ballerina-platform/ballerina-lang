string output = "";

type Employee record {
    readonly int id;
    string name;
    float salary;
};

table<Employee> data = table key(id)[
        { id: 1, name: "Mary", salary: 300.5 },
        { id: 2, name: "John", salary: 200.5 },
        { id: 3, name: "Jim", salary: 330.5 }
    ];

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

    table<Employee> d = table key(id) [];

    int i = 0;
    foreach var {id, name, salary} in d {
    concatIntIntStringFloat(i, id, name, salary);
        i += 1;
    }
    return output;
}

function testIterationOverKeylessTable() returns boolean {
    table<Employee> data = table [
            { id: 1, name: "Mary", salary: 300.5 },
            { id: 2, name: "John", salary: 200.5 },
            { id: 3, name: "Jim", salary: 330.5 }
        ];
    Employee[] ar = [];
    foreach var v in data {
        ar.push(v);
    }
   return ar.length() == 3 && ar[0].name == "Mary" && ar[1].name == "John" && ar[2].name == "Jim";
}
