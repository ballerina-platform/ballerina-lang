function testUndeclaredAttributeAccess() {
	string name;
	Department dpt;
	
	dpt.id = "HR";
}

type Department {
	string dptName;
	int count;
}