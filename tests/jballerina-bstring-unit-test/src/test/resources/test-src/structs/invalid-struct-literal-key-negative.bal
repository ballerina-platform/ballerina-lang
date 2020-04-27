type Department record {
    string dptName = "";
    boolean y = false;
};

type person record {
    string a = "";
};

function testInvalidFieldNameInit () {
    string name;
    Department dpt = {dptName[0]:54};
}

function testVarRefAsKey() {
    person p = {(a): "HR"};
}

function testFuncCallAsKey() {
    person p = {foo(): "HR"};
}

function foo() returns (string) {
	return "dptName";
}