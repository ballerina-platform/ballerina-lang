type Department sealed record {
    string dptName;
    boolean y;
};

type Department sealed record {
    string name;
    int[] a1;
};

type Department2 sealed record {
    string dptName;
    int id;
    string id;
};

function testUndeclaredStructInit () {
    Department123 dpt1 = {name:"HR"};
}

function testUndeclaredStructAccess () {
    string name;
    dpt1.name = "HR";
}

function testUndeclaredAttributeAccess () {
    string name;
    Department dpt;
    dpt.id = "HR";
}

function testUndeclaredAttributeinit () {
    string name;
    Department dpt = {dptName:"HR", age:20};
}

function testInvalidTypeAttributeinit () {
    string name;
    Department dpt = {dptName:54};
}
