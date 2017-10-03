function testInvalidTypeAttributeinit() {
	string name;
	Department dpt = {dptNames:54};
}

struct Department {
	map dptNames;
	int count;
}