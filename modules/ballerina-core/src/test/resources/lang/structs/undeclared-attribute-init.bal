function testUndeclaredAttributeinit() {
	string name;
	Department dpt = {dptName:"HR", age:20};
}

struct Department {
	string dptName;
	int count;
}