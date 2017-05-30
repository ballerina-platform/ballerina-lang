function testInvalidTypeAttributeinit() {
	string name;
	Department dpt = {dptName:54};
}

struct Department {
	string dptName;
	int count;
}