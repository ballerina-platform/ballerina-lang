type Department {
    string dptName;
    boolean y;
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