function testInvalidTypeAttributeinit() {
	string name;
	Department dpt = {dptName[0]:54};
}

struct Department {
	string[] dptName;
	int count;
}