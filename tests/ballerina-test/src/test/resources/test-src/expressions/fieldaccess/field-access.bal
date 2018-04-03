function foo () returns any {
    Address adrs = {city:"Colombo"};
    error e = {message:"custom error"};
    Person prsn = {address : adrs};
	Person|error p = prsn;
	string|error|() x = p!.address!.city;
	return x;
}

struct Person {
	int a;
	string fname = "John";
	string lname;
	Address|error address;
}

struct Address {
	string street;
	string city;
	string country = "Sri Lanka";
}

struct Foo {
}