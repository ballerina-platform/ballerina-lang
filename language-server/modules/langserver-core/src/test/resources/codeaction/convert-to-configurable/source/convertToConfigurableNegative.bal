// invalid qualifiers
final int i = 12;
isolated string[] arr = [];
configurable int[] ports = [];

// unsupported types
int|string|future<int> isf = 3455;
int|string:RegExp ir = 123;
Person person = {
    name: "Anna",
    age: 30,
    i: ()
};

// List constructor negative
int[] iArr = [];
int[] jArr = [];

function updateInts() {
    iArr.push(1);
    jArr[0] = 12;
}

// Table constructor negative
table<map<string>> tbl1 = table [];

function updateTable() {
    tbl1.put({key1: "Value"});
}

// Mapping constructor negative
map<int> portMap1 = {};
map<int> portMap2 = {};

function updateMapping() {
    _ = portMap1.remove("stage");
    portMap2["dev"] = 4000;
}

type Person record {
    string name;
    int age;
    stream<string> i;
};

int valueReAssigned = 1234;

function valueReAssign() {
    valueReAssigned = 123;
}

var host = "localhost";
port = 9000;
