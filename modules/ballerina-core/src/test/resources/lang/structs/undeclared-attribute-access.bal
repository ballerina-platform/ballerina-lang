public function testUndeclaredAttributeAccess() {
	string name;
	Department dpt;
	
	dpt.id = "HR";
}

public type Department {
	string dptName;
	int count;
}