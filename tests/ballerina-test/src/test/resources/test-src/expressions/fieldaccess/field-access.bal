function foo () {
	Person|() p = {};
	string|error|() x = p!.address.city;
}

struct Person {
	int a;
	string fname = "John";
	string lname;
	Address|() address;
}

struct Address {
	string street;
	string city;
	string country = "Sri Lanka";
}

struct Foo {
}