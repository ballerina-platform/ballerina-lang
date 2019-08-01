
// --------  Test the for each arrays typed binding patterns  ---------

function testArrayWithSimpleVariableWithType() {
    string tstStr = "";
    string[] arr = ["A"];
    int i = 0;
    foreach string str in arr {
        tstStr = str;
    }
}

function testArrayWithTupleWithoutType() {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

    foreach var [iVal, sVal] in arr {
        string sV = sVal;
    }
}

function testArrayWithTupleWithType() {
    [int, string][] arr = [[1, "A"], [2, "B"], [3, "C"]];

    foreach [int, string] [iVal, sVal] in arr {
        string sV = sVal;
    }
}

function testArrayWithTupleInTupleWithoutType() {
    [int, [string, float]][] arr = [[1, ["A", 2.0]]];

    foreach var [iVal, [sVal, fVal]] in arr {
        float f = fVal;
    }
}

function testArrayWithTupleInTupleWithType() {
    [int, [string, float]][] arr = [[1, ["A", 2.0]]];

    foreach [int, [string, float]] [iVal, [sVal, fVal]] in arr {
        float f = fVal;
    }
}

function testArrayWithRecordInTupleWithoutType() {
    Data d1 = { i: 1, v: "A" };

    [int, Data][] arr = [[1, d1]];

    foreach var [i, {i: j, v: k}] in arr {
        string kVal = k;
    }
}

function testArrayWithRecordInTupleWithType() {
    Data d1 = { i: 1, v: "A" };
    [int, Data][] arr = [[1, d1]];

    foreach [int, Data] [i, {i: j, v: k}] in arr {
        string kVal = k;
    }
}

function testArrayWithRecordWithoutType() {
    Data d1 = { i: 1, v: "A" };
    Data[] arr = [d1];

    foreach var {i, v} in arr {
        string vVal = v;
    }
}

function testArrayWithRecordWithType() {
    Data d1 = { i: 1, v: "A" };
    Data[] arr = [d1];

    foreach Data {i, v} in arr {
        string vVal = v;
    }
}

function testArrayWithRecordInRecordWithoutType() {
    Data d11 = { i: 1, v: "A" };
    Data2 d21 = { i: 1, v: d11 };
    Data2[] arr = [d21];

    foreach var {i, v: {i: j, v: k}} in arr {
        string kVal = k;
    }
}

function testArrayWithRecordInRecordWithType() {
    Data d11 = { i: 1, v: "A" };
    Data2 d21 = { i: 1, v: d11 };
    Data2[] arr = [d21];

    foreach Data2 {i, v: {i: j, v: k}} in arr {
        string kVal = k;
    }
}

function testArrayWithTupleInRecordWithoutType() {
    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3[] arr = [d1];

    foreach var {i, v: [j, k]} in arr {
        string kVal = k;
    }
}

function testArrayWithTupleInRecordWithType() {
    Data3 d1 = { i: 1, v: [1, "A"] };
    Data3[] arr = [d1];

    foreach Data3 {i, v: [j, k]} in arr {
        string kVal = k;
    }
}

type Data record {
    int i;
    string v;
};

type Data2 record {
    int i;
    Data v;
};

type Data3 record {
    int i;
    [int, string] v;
};

type Protocols record {
    string pData;
    Protocol[] plist;
};

type Protocol record {
    string name;
    string url;
};

// function testJSONToStructCast () returns string|error {
//     json j = {data:"data", plist:[{name:"a", url:"h1"}, {name:"b", url:"h2"}]};
//     var result = check Protocols.convert(j);
//     foreach var protocol in result.plist {
//     }
//     return "";
// }

function testMapWithArityOne () returns (string) | error {
    map<string> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    string val;
    // case covers goto def of map as well
    foreach var v in m {
        val = <string> v;
    }
    return "";
}

function testTupleInRecordWithoutType() {
    Data3 d3 = { i: 1, v: [1, "A"] };
    int i = 0;
    foreach var v in d3 {
        i += 1;
    }
}

type Employee2 record {
    int id;
    string name;
    int salary;
};

table<Employee2> eTable = table {
    { key id, name, salary },
    [
        { 1, "Mary",  300 },
        { 2, "John",  200 },
        { 3, "Jim", 330 }
    ]
};

function testTableWithType() {
    int i = 0;
    foreach var {id, name, salary} in eTable {
        i += salary;
    }
}