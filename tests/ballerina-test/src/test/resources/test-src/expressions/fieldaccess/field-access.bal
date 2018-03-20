function foo () {
	Person|null p = {};
	string|error|null x = p!.address!.city;
}

struct Person {
	int a;
	string fname = "John";
	string lname;
	Address|null address;
}

struct Address {
	string street;
	string city;
	string country = "Sri Lanka";
}

struct Foo {
}