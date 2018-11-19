type Department record {
    string dptName = "";
    boolean y = false;
};

function testVarRefAsKey() {
    Department p = {(a): "HR"};
}

function testFuncCallAsKey() {
    Department	 p = {foo(): "HR"};
}

function foo() returns (string) {
	return "dptName";
}