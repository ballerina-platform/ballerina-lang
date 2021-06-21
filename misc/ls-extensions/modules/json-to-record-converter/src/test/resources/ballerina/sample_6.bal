type Author record {
	string country;
	string period;
	string name;
};

type BooksItem record {
	Author author;
	string name;
};

type State record {
	string code;
	string name;
};

type Address record {
	string number;
	string city;
	string street;
	string neighborhood;
	State state;
};

type SportsItem record {
	string position;
	string sport;
};

type NewRecord record {
	BooksItem[] books;
	Address address;
	SportsItem[] sports;
	string school;
	string year;
	string name;
	int age;
	boolean honors;
};

